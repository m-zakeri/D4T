def find_all_paths(graph, start, end, path=[]):
    path = path + [start]
    if start == end:
        return [path]
    if start not in graph:
        return []
    paths = []
    for node in graph[start]:
        if node not in path:
            newpaths = find_all_paths(graph, node, end, path)
            for newpath in newpaths:
                paths.append(newpath)
    return paths





if __name__ == "__main__":
    graph = {'D1': {'D2': 1, 'C1': 1},
             'D2': {'C2': 1, 'D1': 1},
             'C1': {'C2': 1, 'B1': 1, 'D1': 1},
             'C2': {'D2': 1, 'C1': 1, 'B2': 1},
             'B1': {'C1': 1, 'B2': 1},
             'B2': {'B1': 1, 'A2': 1, 'C2': 1},
             'A2': {'B2': 1, 'A1': 1},
             'A1': {'A2': 1}}
    print(find_all_paths(graph, 'D1', 'A1'))