from utils import Path

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
    interface_info = {
        "path": "E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com",
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

    base_dirs = []
    base_dirs.append('E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\')
    factory_info = {
        'factory': {
            'index': 0,
            'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\creator\\Creator.java',
            'class_name': 'Creator',
            'package': 'com.creator'
        },
        'products': {
            'classes':
                [
                    {
                        'index': 3,
                        'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\products\\JpegReader.java',
                        'class_name': 'JpegReader',
                        'package': 'com.products'
                    },
                    {
                        'index': 2,
                        'path': 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\refactored_project\\javaproject\\com\\products\\GifReader.java',
                        'class_name': 'GifReader',
                        'package': 'com.products'
                    }
                ],
            'methods':
                [
                    {
                        'name': 'getDecodeImage',
                        'return_type': 'DecodedImage',
                        'formal_parameters':
                            [
                                ['int', 'x'],
                                ['int', 'y'],
                                ['bool', 'z']
                            ]
                    }
                ]
        }
    }

    interface_info2 = InterfaceAdapter.convert_factory_info_to_interface_info(factory_info, base_dirs, "IAdder")

    ic = InterfaceCreator(interface_info)
    ic.save()
    print(ic.get_import_text())

