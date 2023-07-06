c = 0
with open('temp.txt') as f:
    for line in f:
        c += int(line.split()[0])
print(c)