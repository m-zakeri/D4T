from antlr4 import *
from gen.JavaLexer import JavaLexer
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled
from antlr4.TokenStreamRewriter import TokenStreamRewriter

from utils import Path

class InterfaceInfoListener(JavaParserLabeledListener):
    def __init__(self):
        self.interface_info = {
            'package':None,
            'name':None,
            'path':None,
            'methods':[]
        }
        self.current_class = None

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.interface_info['package'] = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if ctx.parentCtx.classOrInterfaceModifier() != None:
            print(ctx.parentCtx.getText())
            if len(ctx.parentCtx.classOrInterfaceModifier()) > 0:
                if ctx.parentCtx.classOrInterfaceModifier()[0].getText() == "public":
                    self.current_class = ctx.IDENTIFIER().getText()
                    self.interface_info['name'] = self.current_class
            else:
                self.current_class = ctx.IDENTIFIER().getText()
                self.interface_info['name'] = self.current_class

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = None

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        for modifier in ctx.parentCtx.parentCtx.modifier():
            if modifier.classOrInterfaceModifier() != None:
                #print('modifier.classOrInterfaceModifier().getText()', modifier.classOrInterfaceModifier().getText())
                if modifier.classOrInterfaceModifier().getText() not in ['private']:
                    method = {
                        'name':None,
                        'return_type':None,
                        'formal_parameters':[]
                    }
                    method['name'] = ctx.IDENTIFIER().getText()
                    method['return_type'] = ctx.typeTypeOrVoid().getText()
                    if ctx.formalParameters().formalParameterList() != None:
                        for f in ctx.formalParameters().formalParameterList().formalParameter():
                            _type = f.typeType().getText()
                            identifier = f.variableDeclaratorId().getText()
                            method['formal_parameters'].append([_type, identifier])
                    self.interface_info['methods'].append(method)

    def get_interface_info(self):
        return self.interface_info

class AddingImplementStatementToClass(JavaParserLabeledListener):
    def __init__(self, common_token_stream, class_name, interface_package, interface_name):
        self.common_token_stream = common_token_stream
        self.class_name = class_name
        self.interface_package = interface_package
        self.interface_name = interface_name
        self.last_import_token_index = None
        self.implement_token_index = None
        self.implement_state = []

        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.last_import_token_index = ctx.stop.tokenIndex

    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        self.last_import_token_index = ctx.stop.tokenIndex

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if ctx.IDENTIFIER().getText() == self.class_name:
            self.implement_token_index = ctx.IDENTIFIER().symbol.tokenIndex
            if ctx.EXTENDS() != None:
                self.implement_state.append(ctx.EXTENDS().getText())
                self.implement_token_index = ctx.typeType().stop.tokenIndex
            if ctx.IMPLEMENTS() != None:
                self.implement_state.append(ctx.IMPLEMENTS().getText())
                self.implement_token_index = ctx.typeList().typeType()[-1].stop.tokenIndex

    def exitCompilationUnit(self, ctx:JavaParserLabeled.CompilationUnitContext):
        import_text = f"\nimport {self.interface_package}.{self.interface_name};"
        self.token_stream_rewriter.insertAfter(
            self.last_import_token_index,
            import_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

        if 'implements' in self.implement_state:
            implement_text = f",{self.interface_name}"
        else:
            implement_text = f" implements {self.interface_name}"
        self.token_stream_rewriter.insertAfter(
            self.implement_token_index,
            implement_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )



class InterfaceCreator:
    def __init__(self, interface_info):
        self.interface_info = interface_info

    def make_body(self):
        interface_text = 'package ' + self.interface_info['package'] + ';\n'
        interface_text += "public interface " + self.interface_info['name'] + "{"
        for method in self.interface_info['methods']:
            interface_text += "\n\t" + 'public ' + method['return_type'] + ' ' + method['name'] + '('
            for formalParameter in method['formal_parameters']:
                interface_text += formalParameter[0] + ' ' + formalParameter[1] + ', '
            if method['formal_parameters'] != []:
                interface_text = interface_text[:-2]
            interface_text += ');'
        interface_text += "\n}\n\n"
        return interface_text

    def save(self):
        interface_text = self.make_body()
        with open(self.interface_info['path'] + '\\' + self.interface_info['name'] + '.java', "w") as write_file:
            write_file.write(interface_text)

    def get_import_text(self):
        return self.interface_info['package'] + '.' + self.interface_info['name']

    def add_implement_statement_to_class(self, class_path):
        stream = FileStream(class_path, encoding='utf8')
        lexer = JavaLexer(stream)
        token_stream = CommonTokenStream(lexer)
        parser = JavaParserLabeled(token_stream)
        parser.getTokenStream()
        parse_tree = parser.compilationUnit()
        listener = AddingImplementStatementToClass(
            common_token_stream=token_stream,
            class_name=Path.get_file_name_from_path(class_path),
            interface_package=self.interface_info['package'],
            interface_name=self.interface_info['name']
        )
        walker = ParseTreeWalker()
        walker.walk(t=parse_tree, listener=listener)

        with open(class_path, mode='w', newline='') as f:
            f.write(listener.token_stream_rewriter.getDefaultText())


class InterfaceAdapter:
    @staticmethod
    def convert_factory_info_to_interface_info(factory_info, base_dirs, name):
        interface_info = {}
        interface_info['name'] = name

        all_paths = [factory_info['factory']['path']]
        for product_info in factory_info['products']['classes']:
            all_paths.append(product_info['path'])
        path = Path.detect_path(Path.convert_str_paths_to_list_paths(all_paths))
        interface_info['path'] = path

        package = Path.get_default_package(base_dirs, path + '\\' + name + '.java')
        interface_info['package'] = package

        interface_info['methods'] = factory_info['products']['methods']
        return interface_info

if __name__ == "__main__":

    # interface_info = {
    #     "path": "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com",
    #     "package": 'com.adder',
    #     "name": 'IAdder',
    #     "methods":[
    #         {
    #             "name": "add",
    #             "return_type": 'int',
    #             'formal_parameters':[
    #                 ['int', 'x'],
    #                 ['int', 'y']
    #             ]
    #         }
    #     ]
    # }

    class_path = "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\benchmarks\\simple_injection\\src\\calculator\\Calculator.java"
    class_path = "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\simple_injection\\src\\calculator\\adder\\Adder.java"
    stream = FileStream(r"" + class_path, encoding='utf8')
    lexer = JavaLexer(stream)
    tokens = CommonTokenStream(lexer)
    parser = JavaParserLabeled(tokens)
    tree = parser.compilationUnit()
    listener = InterfaceInfoListener()
    walker = ParseTreeWalker()
    walker.walk(
        listener=listener,
        t=tree
    )
    interface_info = listener.get_interface_info()

    # base_dirs = []
    # base_dirs.append('E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\')
    # factory_info = {
    #     'factory': {
    #         'index': 0,
    #         'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\creator\\Creator.java',
    #         'class_name': 'Creator',
    #         'package': 'com.creator'
    #     },
    #     'products': {
    #         'classes':
    #             [
    #                 {
    #                     'index': 3,
    #                     'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\products\\JpegReader.java',
    #                     'class_name': 'JpegReader',
    #                     'package': 'com.products'
    #                 },
    #                 {
    #                     'index': 2,
    #                     'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\products\\GifReader.java',
    #                     'class_name': 'GifReader',
    #                     'package': 'com.products'
    #                 }
    #             ],
    #         'methods':
    #             [
    #                 {
    #                     'name': 'getDecodeImage',
    #                     'return_type': 'DecodedImage',
    #                     'formal_parameters':
    #                         [
    #                             ['int', 'x'],
    #                             ['int', 'y'],
    #                             ['bool', 'z']
    #                         ]
    #                 }
    #             ]
    #     }
    # }
    #
    # interface_info2 = InterfaceAdapter.convert_factory_info_to_interface_info(factory_info, base_dirs, "IAdder")
    interface_info['name'] = 'I' + interface_info['name']
    path_list = Path.convert_str_paths_to_list_paths([class_path])
    interface_info['path'] = '\\'.join(path_list[0][:-1])
    ic = InterfaceCreator(interface_info)
    ic.save()
    ic.add_implement_statement_to_class(class_path)
    #print(ic.get_import_text())

