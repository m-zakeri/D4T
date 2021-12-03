# i create this file for checking the parameters of the class in python
class x:
    p = 0

    def inc_p(self):
        self.p += 1

if __name__ == '__main__':
    a = x()
    print('a: ')
    print(a.p)
    a.inc_p()
    print(a.p)

    b = x()
    print('b: ')
    print(b.p)
    b.inc_p()
    b.inc_p()
    print(b.p)