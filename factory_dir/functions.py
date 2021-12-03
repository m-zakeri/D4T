import os
import json


def get_path_and_className_from_nodeName(nodeName):
    nodeName = nodeName.split('\\')
    className = nodeName[-1]
    path = '\\'.join(nodeName[:-1])
    return path, className

def compare_similarity_of_two_list(list1, list2):
    return list(set(list1) & set(list2))


def find_products(parent_class, method_class_dic, percentage):
    result = {'factory_dir': int(parent_class), 'products':{'classes':[], 'methods':[]}}

    for c1 in method_class_dic.keys():
        class_list = []
        method_list = method_class_dic[c1]

        len_c1_methods = len(method_class_dic[c1])
        for c2 in method_class_dic.keys():
            len_c2_methods = len(method_class_dic[c2])
            method_list_help = compare_similarity_of_two_list(method_list, method_class_dic[c2])
            if max(len_c1_methods,len_c2_methods) == 0:
                continue

            if len(method_list_help) / max(len_c1_methods,len_c2_methods) >= percentage:
                method_list = method_list_help.copy()
                class_list.append(c2)

        if len(class_list) > len(result['products']['classes']):
            result['products']['classes'] = class_list
            for m in method_list:
                if method_class_dic[class_list[0]][m] != {}:
                    result['products']['methods'].append(method_class_dic[class_list[0]][m])
    return result

def find_all_file(address, type):
    all_files = []
    for root, dirs, files in os.walk(address):
        for file in files:
            if file.endswith('.'+type):
                all_files.append(os.path.join(root, file))
    return all_files

def find_path_of_import(path, _import):
    _import = _import.split('.')
    path = path.split('\\')
    candidate_path = []
    for d in range(len(path) - 1):
        if path[d] == _import[0]:
            candidate_path.append('\\'.join(path[:d] + _import) + '.java')
        elif path[d] == path[-2]:
            candidate_path.append('\\'.join(path[:-1] + _import) + '.java')
    for p in candidate_path:
        try:
            file = open(''+p, 'r')
            file.close()
            return p
        except:
            pass

def find_all_path_of_import_star(path, imports_star):
    path = path.split('\\')
    candidate_path = []
    for import_star in imports_star:
        import_star = import_star.split('.')
        for d in range(len(path)):
            if path[d] == import_star[0]:
                candidate_path.append('\\'.join(path[:d] + import_star))

    result = []
    for c in candidate_path:
        result += find_all_file(c, 'java')
    return result



def find_path_of_class_in_type_of_import(import_list, class_name):
    class_name = class_name.split('.')
    for _import in import_list:
        if _import[:4] != 'java':
            if _import.split('.')[-1] == class_name[0]:
                result = _import
                for c in class_name[1:]:
                    result += '.' + c
                return result

def make_graph(class_dic, imports, imports_star, path):
    result = {'path': path, 'classes': {}}

    for c in class_dic.keys():
        data = []
        for i in class_dic[c]:
            path_of_class_in_type_of_import = find_path_of_class_in_type_of_import(imports, i)
            if path_of_class_in_type_of_import != None:
                path_of_import = find_path_of_import(path, find_path_of_class_in_type_of_import(imports, i))
                if path_of_import != None:
                    if not path_of_import in data:
                        data.append(path_of_import)
        import_star_paths = find_all_path_of_import_star(path, imports_star)
        for i in class_dic[c]:
            for pp in import_star_paths:
                if get_class_name_from_path(pp) == i:
                    data.append(pp)
                    break
        result['classes'][c] = data
    return result

def get_class_name_from_path(path):
    path = path.split('\\')
    class_name = path[-1]
    class_name = class_name.split('.')
    class_name = class_name[0]
    return  class_name

def find_path_from_id(result):
    with open('class_name_index.json', 'r') as file:
        json_list = json.load(file)
        json_list = list(json_list.keys())
    result['factory_dir'] = json_list[int(result['factory_dir'])]
    products_class_list = []
    for product_class in result['products']['classes']:
        products_class_list.append(json_list[int(product_class)])
    result['products']['classes'] = products_class_list
    return result

