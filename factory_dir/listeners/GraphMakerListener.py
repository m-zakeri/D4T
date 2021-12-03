from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener


class GraphMakerListener(JavaParserLabeledListener):
    def __init__(self):
        self.currentClass = ''
        self.class_dic = {}
        self.imports = []
        self.imports_star = []

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.currentClass = ctx.IDENTIFIER().getText()
        self.class_dic[self.currentClass] = []

    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassOrInterfaceType(self, ctx:JavaParserLabeled.ClassOrInterfaceTypeContext):
        if len(ctx.IDENTIFIER()) > 0 and self.currentClass != '':
            text = ''
            for t in ctx.IDENTIFIER():
                text += t.getText() + '.'
            self.class_dic[self.currentClass].append(text[:-1])
