from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener


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