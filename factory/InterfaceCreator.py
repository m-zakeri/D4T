
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
            interfaceText += ');'
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
        all_paths = [result['factory']] + result['products']['classes']
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