import json
import networkx as nx

from antlr4 import *
from antlr4.TokenStreamRewriter import TokenStreamRewriter
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled

from interface import InterfaceAdapter, InterfaceCreator
from utils import get_parser, get_parser_and_tokens


class FixCreatorListener(JavaParserLabeledListener):
    def __init__(self, interface_name, interface_import_text,
                 common_token_stream: CommonTokenStream = None,
                 creator_identifier: str = None,
                 products_identifier: str = None, ):
        self.interface_import_text = interface_import_text
        self.token_stream = common_token_stream
        self.creator_identifier = creator_identifier
        self.products_identifier = products_identifier
        self.interfaceName = interface_name
        self.inCreator = False
        self.packageIndex = 0
        self.productVarTypeIndex = []
        self.productVarValueIndex = []
        self.productConstructorMethod = []
        self.currentClass = None
        # Move all the tokens in the source code in a buffer, token_stream_rewriter.
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

        self.creator_start_index = None

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if ctx.IDENTIFIER().getText() == self.creator_identifier:
            self.inCreator = True
            self.creator_start_index = ctx.classBody().start.tokenIndex
            self.currentClass = ctx.IDENTIFIER().symbol.text

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.inCreator = False

    def enterLocalVariableDeclaration(self, ctx: JavaParserLabeled.LocalVariableDeclarationContext):
        if self.inCreator:
            if ctx.typeType().classOrInterfaceType() is None:
                variable_type = ctx.variableDeclarators().variableDeclarator()[0].variableDeclaratorId().IDENTIFIER()
            else:
                variable_type = ctx.typeType().classOrInterfaceType().IDENTIFIER(0)
            if variable_type.symbol.text in self.products_identifier:
                self.productVarTypeIndex.append(variable_type.symbol.tokenIndex)
                if ctx.variableDeclarators().variableDeclarator(0).ASSIGN() is not None:
                    self.productVarValueIndex.append([variable_type.symbol.text,
                                                      ctx.variableDeclarators().variableDeclarator(
                                                          0).ASSIGN().symbol.tokenIndex, ctx.stop.tokenIndex])

    def enterFieldDeclaration(self, ctx: JavaParserLabeled.FieldDeclarationContext):
        if self.inCreator:
            if (ctx.typeType().classOrInterfaceType() is not None) and \
                    (ctx.variableDeclarators().variableDeclarator(0).ASSIGN() is not None):
                variable_type = ctx.typeType().classOrInterfaceType().IDENTIFIER(0)
                if variable_type.symbol.text in self.products_identifier:
                    self.productVarTypeIndex.append(variable_type.symbol.tokenIndex)
                    self.productVarValueIndex.append([variable_type.symbol.text,
                                                      ctx.variableDeclarators().variableDeclarator(
                                                          0).ASSIGN().symbol.tokenIndex,
                                                      ctx.stop.tokenIndex])

    def exitPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.packageIndex = ctx.SEMI().symbol.tokenIndex

    def exitCompilationUnit(self, ctx: JavaParserLabeled.CompilationUnitContext):
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                               index=self.packageIndex,
                                               text='\n' + self.interface_import_text + '\n')

        for item in self.productVarTypeIndex:
            self.token_stream_rewriter.replace(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                               from_idx=item,
                                               to_idx=item,
                                               text=self.interfaceName)

        new_product_method = "\n"
        for item in self.productConstructorMethod:
            new_product_method += item
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                               index=self.creator_start_index,
                                               text=new_product_method)


class FixProductsListener(JavaParserLabeledListener):
    def __init__(self, interface_name, interface_import_text,
                 common_token_stream: CommonTokenStream = None,
                 products_identifier: str = None):
        self.interface_import_text = interface_import_text
        self.token_stream = common_token_stream
        self.products_identifier = products_identifier
        self.interfaceName = interface_name
        self.packageIndex = 0
        # Move all the tokens in the source code in a buffer, token_stream_rewriter.
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if ctx.IDENTIFIER().getText() in self.products_identifier:
            if ctx.typeList():
                text = ", " + self.interfaceName
            else:
                text = " implements " + self.interfaceName

            self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                   index=ctx.classBody().start.tokenIndex - 1,
                                                   text=text)

    def exitPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.packageIndex = ctx.SEMI().symbol.tokenIndex

    def exitCompilationUnit(self, ctx: JavaParserLabeled.CompilationUnitContext):
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                               index=self.packageIndex,
                                               text='\n' + self.interface_import_text + '\n')


class ProductCreatorDetectorListener(JavaParserLabeledListener):
    def __init__(self, class_name):
        self.methods = {}
        self.current_class = ''
        self.current_method_info = {}
        self.class_name = class_name
        self.current_class_body_public = None
        self.package = None

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if ctx.IMPLEMENTS() is None:
            self.current_class = ctx.IDENTIFIER().getText()

    def enterMethodDeclaration(self, ctx: JavaParserLabeled.MethodDeclarationContext):
        self.current_method_info = {}
        if self.current_class == self.class_name:
            if self.current_class_body_public is not None:
                if self.current_class_body_public.getText() == ctx.getText():
                    self.current_method_info['name'] = ctx.IDENTIFIER().getText()
                    self.current_method_info['return_type'] = ctx.typeTypeOrVoid().getText()
                    self.current_method_info['formal_parameters'] = []
                    self.methods[ctx.IDENTIFIER().getText()] = {}

    def exitMethodDeclaration(self, ctx: JavaParserLabeled.MethodDeclarationContext):
        if len(self.current_method_info.keys()) > 0:
            self.methods[self.current_method_info['name']] = self.current_method_info

    def enterFormalParameter(self, ctx: JavaParserLabeled.FormalParameterContext):
        if 'formal_parameters' in self.current_method_info.keys():
            formal_parameter_info = list()
            formal_parameter_info.append(ctx.typeType().getText())
            formal_parameter_info.append(ctx.variableDeclaratorId().getText())
            self.current_method_info['formal_parameters'].append(formal_parameter_info)

    def enterClassBodyDeclaration2(self, ctx: JavaParserLabeled.ClassBodyDeclaration2Context):
        if self.current_class == self.class_name:
            if len(ctx.modifier()) > 0:
                if ctx.modifier()[0].getText() == 'public':
                    self.current_class_body_public = ctx.memberDeclaration()


class Factory:
    def __fix_creator(self, creator_path, interface_import_text, interface_name, creator_identifier,
                      products_identifier):
        parser, token_stream = get_parser_and_tokens(creator_path)
        parse_tree = parser.compilationUnit()
        my_listener = FixCreatorListener(interface_name=interface_name,
                                         interface_import_text=interface_import_text,
                                         common_token_stream=token_stream,
                                         creator_identifier=creator_identifier,
                                         products_identifier=products_identifier)
        walker = ParseTreeWalker()
        walker.walk(t=parse_tree, listener=my_listener)

        with open(creator_path, mode='w', newline='', encoding='utf8', errors='ignore') as f:
            f.write(my_listener.token_stream_rewriter.getDefaultText())

    def __fix_product(self, product_path, interface_import_text, interface_name, creator_identifier,
                      products_identifier):
        parser, token_stream = get_parser_and_tokens(product_path)
        parse_tree = parser.compilationUnit()
        my_listener = FixProductsListener(
            interface_name=interface_name,
            interface_import_text=interface_import_text,
            common_token_stream=token_stream,
            products_identifier=products_identifier
        )
        walker = ParseTreeWalker()
        walker.walk(t=parse_tree, listener=my_listener)

        with open(product_path, mode='w', newline='', encoding='utf8', errors='ignore') as f:
            f.write(my_listener.token_stream_rewriter.getDefaultText())

    def __get_intersection_of_two_list(self, list1, list2):
        return list(set(list1) & set(list2))

    def find_products(self, parent_class, method_class_dic, sensitivity):
        result = {'factory': int(parent_class), 'products': {'classes': [], 'methods': []}}
        best_factory_quality = 0
        for c1 in method_class_dic.keys():
            class_list = []
            method_list = method_class_dic[c1]

            len_c1_methods = len(method_class_dic[c1])
            for c2 in method_class_dic.keys():
                len_c2_methods = len(method_class_dic[c2])
                method_list_help = self.__get_intersection_of_two_list(method_list, method_class_dic[c2])
                if max(len_c1_methods, len_c2_methods) == 0:
                    continue
                if len(method_list_help) / len(set(method_class_dic[c1] + method_class_dic[c2])) >= sensitivity:
                    method_list = method_list_help.copy()
                    class_list.append(c2)
            # if len(class_list) > len(result['products']['classes']):
            #     result['products']['classes'] = class_list
            #     for m in method_list:
            #         if method_class_dic[class_list[0]][m] != {}:
            #             result['products']['methods'].append(method_class_dic[class_list[0]][m])
            common_methods = set.intersection(*[set(method_class_dic[c2]) for c in class_list])
            union_methods = set.union(*[set(method_class_dic[c2]) for c in class_list])
            if len(common_methods) / len(union_methods) > best_factory_quality:
                best_factory_quality = len(common_methods) / len(union_methods)
                result['products']['classes'] = class_list
                for m in method_list:
                    if method_class_dic[class_list[0]][m] != {}:
                        result['products']['methods'].append(method_class_dic[class_list[0]][m])
        return result

    @staticmethod
    def __get_class_info_from_index(index, index_dic, index_list):
        class_info = dict()
        class_info['index'] = index
        key = index_list[class_info['index']]
        class_info['path'] = index_dic[key]['path']
        key = key.split("-")
        class_info['class_name'] = key[1]
        class_info['package'] = key[0]
        return class_info

    def find_class_info_from_id(self, result, index_dic):
        index_list = list(index_dic.keys())
        result['factory'] = Factory.__get_class_info_from_index(
            int(result['factory']),
            index_dic,
            index_list
        )
        products_class_list = []
        for product_class in result['products']['classes']:
            product_info = Factory.__get_class_info_from_index(
                int(product_class),
                index_dic,
                index_list
            )
            products_class_list.append(product_info)
        result['products']['classes'] = products_class_list
        return result

    def refactor(self, sensitivity, index_dic, class_diagram, base_dirs, edit=True):
        reports = []
        index_dic_keys = list(index_dic.keys())
        roots = list((v for v, d in class_diagram.in_degree() if d >= 0))
        for r in roots:
            root_dfs = list(nx.bfs_edges(class_diagram, source=r, depth_limit=1))
            if len(root_dfs) > 1:
                method_class_dic = {}
                for child_index in root_dfs:
                    child = index_dic_keys[int(child_index[1])]
                    child_path = index_dic[child]['path']
                    child_class_name = child.split('-')[1]
                    print('child path: ', child_path)
                    parser = get_parser(child_path)

                    tree = parser.compilationUnit()
                    listener = ProductCreatorDetectorListener(child_class_name)
                    walker = ParseTreeWalker()
                    walker.walk(
                        listener=listener,
                        t=tree
                    )
                    method_class_dic[int(child_index[1])] = listener.methods

                result = self.find_products(root_dfs[0][0], method_class_dic, sensitivity)
                if len(result['products']['classes']) > 1:
                    print('--------------------------------------------------')
                    print(json.dumps(result, indent=4))
                    reports.append(result)

                    interface_name = 'Interface' + str(result['factory'])
                    result = self.find_class_info_from_id(result, index_dic)
                    # make interface for
                    interface_info = InterfaceAdapter.convert_factory_info_to_interface_info(result,
                                                                                             base_dirs,
                                                                                             interface_name
                                                                                             )
                    interface_creator = InterfaceCreator(interface_info)
                    if edit:
                        interface_creator.save()
                    creator_path = result['factory']['path']
                    creator_class_name = result['factory']['class_name']
                    products_path = []
                    products_class_name = []
                    for product_info in result['products']['classes']:
                        products_path.append(product_info['path'])
                        products_class_name.append(product_info['class_name'])

                    interface_import_text = 'import ' + interface_creator.get_import_text() + ';'
                    if edit:
                        self.__fix_creator(creator_path, interface_import_text, interface_name, creator_class_name,
                                           products_class_name)
                        for product_path in products_path:
                            self.__fix_product(product_path, interface_import_text, interface_name,
                                               creator_class_name,
                                               products_class_name)
                    print('--------------------------------------------------')
        return reports
