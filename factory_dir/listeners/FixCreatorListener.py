from antlr4 import *
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




