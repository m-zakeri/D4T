from antlr4 import *
from antlr4.TokenStreamRewriter import TokenStreamRewriter

from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled


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


