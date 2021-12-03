import time

import networkx as nx
from distutils.dir_util import copy_tree
from shutil import rmtree

from antlr4 import *
from gen.JavaLexer import JavaLexer
from antlr4.TokenStreamRewriter import TokenStreamRewriter
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled

class FixCreatorListener(JavaParserLabeledListener):
    def __init__(self, interfaceName, interface_import_text,
                 common_token_stream: CommonTokenStream = None,
                 creator_identifier: str = None,
                 products_identifier: str = None,):
        self.interface_import_text = interface_import_text
        self.enter_class = False
        self.token_stream = common_token_stream
        self.creator_identifier = creator_identifier
        self.products_identifier = products_identifier
        self.interfaceName = interfaceName
        self.inCreator = False
        self.inProducts = False
        self.productsMethod = {}
        self.packageIndex = 0
        self.productsClassIndex = []
        self.productVarTypeIndex = []
        self.productVarValueIndex = []
        self.productConstructorMethod = []
        self.productConstructorParam = {}
        self.currentClass = None
        # Move all the tokens in the source code in a buffer, token_stream_rewriter.
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if ctx.IDENTIFIER().getText() == self.creator_identifier:
            self.inCreator = True
            self.CretorStartIndex = ctx.classBody().start.tokenIndex
            self.currentClass = ctx.IDENTIFIER().symbol.text

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.inCreator = False

    def enterLocalVariableDeclaration(self, ctx:JavaParserLabeled.LocalVariableDeclarationContext):
        if self.inCreator == True:
            if ctx.typeType().classOrInterfaceType() == None:
                variableType = ctx.variableDeclarators().variableDeclarator()[0].variableDeclaratorId().IDENTIFIER()
            else:
                variableType = ctx.typeType().classOrInterfaceType().IDENTIFIER(0)
            if variableType.symbol.text in self.products_identifier:
                self.productVarTypeIndex.append(variableType.symbol.tokenIndex)
                if ctx.variableDeclarators().variableDeclarator(0).ASSIGN() != None:
                    self.productVarValueIndex.append([variableType.symbol.text,ctx.variableDeclarators().variableDeclarator(0).ASSIGN().symbol.tokenIndex,ctx.stop.tokenIndex])

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        if self.inCreator == True:
            try:
                variableType = ctx.typeType().classOrInterfaceType().IDENTIFIER(0)
                if variableType.symbol.text in self.products_identifier:
                    self.productVarTypeIndex.append(variableType.symbol.tokenIndex)
                    self.productVarValueIndex.append([variableType.symbol.text,
                                                      ctx.variableDeclarators().variableDeclarator(0).ASSIGN().symbol.tokenIndex,
                                                      ctx.stop.tokenIndex])
            except:
                pass
                #print(ctx.getText())

    def exitPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.packageIndex = ctx.SEMI().symbol.tokenIndex

    def exitCompilationUnit(self, ctx:JavaParserLabeled.CompilationUnitContext):
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                            index=self.packageIndex,
                                            text= '\n' + self.interface_import_text + '\n')

        for item in self.productVarTypeIndex:
            self.token_stream_rewriter.replace(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=item,
                                                  to_idx=item,
                                                  text= self.interfaceName)


        newProductMethod = "\n"
        for item in self.productConstructorMethod:
            newProductMethod += item
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                              index=self.CretorStartIndex,
                                              text= newProductMethod)

class FixProductsListener(JavaParserLabeledListener):
    def __init__(self, interfaceName, interface_import_text,
                 common_token_stream: CommonTokenStream = None,
                 creator_identifier: str = None,
                 products_identifier: str = None,):
        self.interface_import_text = interface_import_text
        self.enter_class = False
        self.token_stream = common_token_stream
        self.creator_identifier = creator_identifier
        self.products_identifier = products_identifier
        self.interfaceName = interfaceName
        self.inCreator = False
        self.inProducts = False
        self.productsMethod = {}
        self.packageIndex = 0
        self.productsClassIndex = []
        self.productVarTypeIndex = []
        self.productVarValueIndex = []
        self.productConstructorMethod = []
        self.productConstructorParam = {}
        self.currentClass = None
        # Move all the tokens in the source code in a buffer, token_stream_rewriter.
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if ctx.IDENTIFIER().getText() in self.products_identifier:
            self.inProducts = True
            try:
                #print(ctx.typeType().classOrInterfaceType().getText())
                self.productsClassIndex.append(ctx.typeType().classOrInterfaceType().IDENTIFIER()[0].symbol.tokenIndex)
            except Exception as e:
                #print(e)
                self.productsClassIndex.append(ctx.IDENTIFIER().symbol.tokenIndex)
            self.currentClass = ctx.IDENTIFIER().symbol.text

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.inProducts = False

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if self.inProducts == True:
            methodModifire = ctx.parentCtx.parentCtx.start.text
            if methodModifire == 'public':
                MethodText = self.token_stream_rewriter.getText(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                                start= ctx.parentCtx.parentCtx.start.tokenIndex,
                                                                stop= ctx.formalParameters().RPAREN().symbol.tokenIndex) + ";"
                if MethodText not in self.productsMethod:
                    self.productsMethod[MethodText] = [self.currentClass]
                else:
                    self.productsMethod[MethodText].append(self.currentClass)


    def enterConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        if self.inProducts == True:
            try:
                Parameter = ""
                if ctx.formalParameters().children.__len__() > 0:
                    ParamChild = ctx.formalParameters().children[1]
                    for i in range (0,ParamChild.children.__len__(),2):
                        Parameter += ParamChild.children[i].stop.text + ","
                    Parameter = Parameter[:-1]

                self.productConstructorParam[self.currentClass] = Parameter

                ParamList = self.token_stream_rewriter.getText(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                                    start= ctx.formalParameters().LPAREN().symbol.tokenIndex,
                                                                    stop= ctx.formalParameters().RPAREN().symbol.tokenIndex)

                Method = "\t" + self.interfaceName + " create" +\
                         self.currentClass + ParamList +\
                         "{\n\t\t" + "return new " + self.currentClass + "(" + Parameter + ");\n\t}\n"

                self.productConstructorMethod.append(Method)
            except:
                pass
                #print(ctx.getText())


    def exitPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.packageIndex = ctx.SEMI().symbol.tokenIndex

    def exitCompilationUnit(self, ctx:JavaParserLabeled.CompilationUnitContext):
        self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                index=self.packageIndex,
                                                text='\n' + self.interface_import_text + '\n')
        for item in self.productsClassIndex:
            self.token_stream_rewriter.insertAfter(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                   index=item,
                                                   text=" implements " + self.interfaceName)

class ProductCreatorDetectorListener(JavaParserLabeledListener):
    def __init__(self, class_name):
        self.methods = {}
        self.current_class = ''
        self.current_method_info = {}
        self.class_name = class_name
        self.current_class_body_public = None
        self.package = None

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if ctx.IMPLEMENTS() == None:
            self.current_class = ctx.IDENTIFIER().getText()

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method_info = {}
        if self.current_class == self.class_name:
            if self.current_class_body_public != None:
                if self.current_class_body_public.getText() == ctx.getText():
                    self.current_method_info['name'] = ctx.IDENTIFIER().getText()
                    self.current_method_info['returnType'] = ctx.typeTypeOrVoid().getText()
                    self.current_method_info['formalParameter'] = []
                    self.methods[ctx.IDENTIFIER().getText()] = {}

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if len(self.current_method_info.keys()) > 0:
            self.methods[self.current_method_info['name']] = self.current_method_info

    def enterFormalParameter(self, ctx:JavaParserLabeled.FormalParameterContext):
        if 'formalParameter' in self.current_method_info.keys():
            formal_parameter_info = []
            formal_parameter_info.append(ctx.typeType().getText())
            formal_parameter_info.append(ctx.variableDeclaratorId().getText())
            self.current_method_info['formalParameter'].append(formal_parameter_info)

    def enterClassBodyDeclaration2(self, ctx:JavaParserLabeled.ClassBodyDeclaration2Context):
        if self.current_class == self.class_name:
            if len(ctx.modifier()) > 0:
                if ctx.modifier()[0].getText() == 'public':
                    self.current_class_body_public = ctx.memberDeclaration()

class InterfaceCreator:

    def __init__(self):
        self.import_text = 'import '

    def make_body(self, name, products):
        interfaceText = "public interface " + name + "{"
        for method in products['methods']:
            interfaceText += "dir\n\t" + 'public ' + method['returnType'] + ' ' + method['name'] + '('
            for formalParameter in method['formalParameter']:
                interfaceText += formalParameter[0] + ' ' + formalParameter[1] + ', '
            if method['formalParameter'] != []:
                interfaceText = interfaceText[:-2]
            interfaceText += ')'
        interfaceText += "\n}\n\n"
        return interfaceText

    def detect_path(self, paths):
        if len(paths) == 1:
            return '\\'.join(paths[0][-2])
        max_path_lengh = max([len(listpath) for listpath in paths])
        for i in range(max_path_lengh):
            x = set([j[i] for j in paths])
            if len(x) > 1 :
                return '\\'.join(paths[0][:i])


    def convert_strpath_to_listpath(self, strpathlist):
        listpathlist = []
        for strpath in strpathlist:
            listpathlist.append(strpath.split('\\'))
        return listpathlist

    def save(self, result, name, package):
        all_paths = [result['factory']['path']]
        for product_info in result['products']['classes']:
            all_paths.append(product_info['path'])
        path = self.detect_path(self.convert_strpath_to_listpath(all_paths))
        # detect import text
        path_list = path.split("\\")
        if package == None:
            self.import_text += '.'.join(path_list) + '.' + name + ';'
        else:
            start_index = path_list.index(package.split('.')[0])
            self.import_text += '.'.join(path_list[start_index:]) + '.' + name + ';'


        inteface_text = self.make_body(name, result['products'])
        with open(path + '\\' + name + '.java', "w") as write_file:
            write_file.write(inteface_text)

    def get_import_text(self):
        return self.import_text

class Factory:
    def __fix_creator(self, creator_path, interface_import_text, interface_name, creator_identifier, products_identifier):
        # print(creator_path)
        stream = FileStream(creator_path, encoding='utf8')
        lexer = JavaLexer(stream)
        token_stream = CommonTokenStream(lexer)
        parser = JavaParserLabeled(token_stream)
        parser.getTokenStream()
        parse_tree = parser.compilationUnit()
        my_listener = FixCreatorListener(interfaceName=interface_name,
                                         interface_import_text=interface_import_text,
                                         common_token_stream=token_stream,
                                         creator_identifier=creator_identifier,
                                         products_identifier=products_identifier)
        walker = ParseTreeWalker()
        walker.walk(t=parse_tree, listener=my_listener)

        with open(creator_path, mode='w', newline='') as f:
            f.write(my_listener.token_stream_rewriter.getDefaultText())

    def __fix_product(self, product_path, interface_import_text, interface_name, creator_identifier, products_identifier):
        stream = FileStream(product_path, encoding='utf8')
        lexer = JavaLexer(stream)
        token_stream = CommonTokenStream(lexer)
        parser = JavaParserLabeled(token_stream)
        parser.getTokenStream()
        parse_tree = parser.compilationUnit()
        my_listener = FixProductsListener(interfaceName=interface_name,
                                          interface_import_text=interface_import_text,
                                          common_token_stream=token_stream,
                                          creator_identifier=creator_identifier,
                                          products_identifier=products_identifier)
        walker = ParseTreeWalker()
        walker.walk(t=parse_tree, listener=my_listener)

        with open(product_path, mode='w', newline='') as f:
            f.write(my_listener.token_stream_rewriter.getDefaultText())

    def __compare_similarity_of_two_list(self, list1, list2):
        return list(set(list1) & set(list2))

    def __find_products(self, parent_class, method_class_dic, percentage):
        result = {'factory': int(parent_class), 'products': {'classes': [], 'methods': []}}

        for c1 in method_class_dic.keys():
            class_list = []
            method_list = method_class_dic[c1]

            len_c1_methods = len(method_class_dic[c1])
            for c2 in method_class_dic.keys():
                len_c2_methods = len(method_class_dic[c2])
                method_list_help = self.__compare_similarity_of_two_list(method_list, method_class_dic[c2])
                if max(len_c1_methods, len_c2_methods) == 0:
                    continue

                if len(method_list_help) / max(len_c1_methods, len_c2_methods) >= percentage:
                    method_list = method_list_help.copy()
                    class_list.append(c2)

            if len(class_list) > len(result['products']['classes']):
                result['products']['classes'] = class_list
                for m in method_list:
                    if method_class_dic[class_list[0]][m] != {}:
                        result['products']['methods'].append(method_class_dic[class_list[0]][m])
        return result

    def __get_class_info_from_index(self, index, index_dic, index_list):
        class_info = {}
        class_info['index'] = index
        key = index_list[class_info['index']]
        class_info['path'] = index_dic[key]['path']
        key = key.split("-")
        class_info['class_name'] = key[1]
        class_info['package'] = key[0]
        return class_info

    def __find_class_info_from_id(self, result, index_dic):
        index_list = list(index_dic.keys())
        result['factory'] = self.__get_class_info_from_index(int(result['factory']),
                                                                 index_dic,
                                                                 index_list)
        products_class_list = []
        for product_class in result['products']['classes']:
            product_info = self.__get_class_info_from_index(int(product_class),
                                                            index_dic,
                                                            index_list)
            products_class_list.append(product_info)
        result['products']['classes'] = products_class_list
        return result

    def detect_and_fix(self, sensitivity, index_dic, class_diagram):
        index_dic_keys = list(index_dic.keys())
        print(len(index_dic_keys))
        roots = list((v for v, d in class_diagram.in_degree() if d >= 0))
        for r in roots:
            root_dfs = list(nx.bfs_edges(class_diagram, source=r, depth_limit=1))
            if len(root_dfs) > 1:
                method_class_dic = {}
                package = None
                for child_index in root_dfs:
                    try:
                        child = index_dic_keys[int(child_index[1])]
                    except:
                        print(len(index_dic_keys))
                        print(int(child_index[1]))
                    child_path = index_dic[child]['path']
                    child_class_name = child.split('-')[1]
                    try:
                        print('child path : ', child_path)
                        stream = FileStream(r"" + child_path)
                    except:
                        print(child_path, 'can not read')
                    lexer = JavaLexer(stream)
                    tokens = CommonTokenStream(lexer)
                    parser = JavaParserLabeled(tokens)
                    tree = parser.compilationUnit()
                    listener = ProductCreatorDetectorListener(child_class_name)
                    walker = ParseTreeWalker()
                    walker.walk(
                        listener=listener,
                        t=tree
                    )
                    method_class_dic[int(child_index[1])] = listener.methods
                    package = listener.package

                result = self.__find_products(root_dfs[0][0], method_class_dic, sensitivity)
                if len(result['products']['classes']) > 1:
                    print('--------------------------------------------------')
                    print(result)
                    interface_name = 'Interface' + str(result['factory'])
                    result = self.__find_class_info_from_id(result, index_dic)
                    print(result)
                    # print(result['factory_dir'])
                    # make interface for
                    interface_creator = InterfaceCreator()
                    interface_creator.save(result, interface_name, package)
                    # print(interface_creator.get_import_text())
                    creator_path = result['factory']['path']
                    creator_className = result['factory']['class_name']
                    products_path = []
                    products_className = []
                    for product_info in result['products']['classes']:
                        products_path.append(product_info['path'])
                        products_className.append(product_info['class_name'])

                    self.__fix_creator(creator_path, interface_creator.get_import_text(), interface_name, creator_className,
                                products_className)
                    for product_path in products_path:
                        self.__fix_product(product_path, interface_creator.get_import_text(), interface_name,
                                    creator_className,
                                    products_className)
                    print('--------------------------------------------------')