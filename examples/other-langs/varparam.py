class O:
    def __set__(self, value):
        print(self, value)

def scope(a):
    a = 3

scope(4)
