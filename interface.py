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

    ic = InterfaceCreator(interface_info)
    ic.save(path)
    print(ic.get_import_text())
