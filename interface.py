from antlr4 import *
from gen.JavaLexer import JavaLexer
from antlr4.TokenStreamRewriter import TokenStreamRewriter
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled

class InterfaceInfoListener(JavaParserLabeledListener):
    def __init__(self):
        self.interface_info = {
            'package':None,
            'name':None,
            'methods':[]
        }
        self.current_class = None

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.interface_info['package'] = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if ctx.parentCtx.classOrInterfaceModifier() != None:
            if ctx.parentCtx.classOrInterfaceModifier().getText() == "public":
                self.current_class = ctx.IDENTIFIER().getText()
                self.interface_info['name'] = self.current_class

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = None

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if ctx.parentCtx.parentCtx.modifier().classOrInterfaceModifier() != None:
            if ctx.parentCtx.parentCtx.modifier().classOrInterfaceModifier().getText() in ["public", "static"]:
                method = {
                    'name':None,
                    'return_type':None,
                    'formal_parameters':[]
                }
                method['name'] = ctx.IDENTIFIER().getText()
                method['return_type'] = ctx.typeTypeOrVoid().getText()
                for f in ctx.formalParameters().formalParmeterList():
                    _type = f.typeType().getText()
                    identifier = f.variableDeclaratorId().getText()
                    method['formal_parameters'].append([_type, identifier])
                self.interface_info['methods'].append(method)
                method = None

    def get_interface_info(self):
        return self.interface_info




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

    def save(self, path):
        interface_text = self.make_body()
        with open(path + '\\' + interface_info['name'] + '.java', "w") as write_file:
            write_file.write(interface_text)

    def get_import_text(self):
        return self.interface_info['package'] + '.' + self.interface_info['name']

if __name__ == "__main__":
    path = "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com"
    interface_info = {
        "package": 'com.adder',
        "name": 'IAdder',
        "methods":[
            {
                "name": "add",
                "return_type": 'int',
                'formal_parameters':[
                    ['int', 'x'],
                    ['int', 'y']
                ]
            }
        ]
    }
    interface_info = None
    class_path = "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\benchmarks\\simple_injection\\src\\calculator\\Calculator.java"
    stream = FileStream(r"" + class_path)
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
    ic = InterfaceCreator(interface_info)
    ic.save(path)
    print(ic.get_import_text())

