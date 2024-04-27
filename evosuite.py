import json

def main():
    with open('class_index.json', 'r') as f:
        class_dict = json.load(f)
    with open('evosuite.txt', 'w') as f:
        classes = ['.'.join(x.replace('-','.').split('.')[:-1]) for x in class_dict.keys()]
        classes = ','.join(classes)
        result = f'mvn evosuite:generate -Dcuts={classes} -DtimeInMinutesPerClass=10'
        f.write(result)


if __name__ == '__main__':
    main()