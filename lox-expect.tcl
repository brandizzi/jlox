#!/usr/bin/expect -f

spawn -noecho java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox badfun.lox
expect "\[line 3\] Error at 'a': Variable with this name already declared in this scope.
\[line 2\] Error at 'a': Local variable never used.
\[line 3\] Error at 'a': Local variable never used."

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox badret.lox
expect "\[line 1\] Error at 'return': Cannot return from top-level code."

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox checkuse.lox
expect "\[line 4\] Error at 'b': Local variable never used."

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox counter.lox
expect "1\NEWLINE2"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox fibonacci2.lox
expect "0
1
1
2
3
5
8
13
21
34
55
89
144
233
377
610
987
1597
2584
4181"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox fibonacci.lox
expect "0
1
1
2
3
5
8
13
21
34
55
89
144
233
377
610
987
1597
2584
4181"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox func.lox
expect "Hi, Dear Reader!"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox funexprstmt.lox
expect "<anon fn>
Operands must be two numbers or two strings.
\[line 3\]"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox hello.lox
expect "Hello World!"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox hello2.lox
expect "Hello World!!"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox hello3.lox
expect "Hello World!!!"


spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox point.lox
expect "2
3"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox scope.lox
expect "inner a
outer b
global c
outer a
outer b
global c
global a
global b
global c"

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox scope1.lox
expect "global
global"


spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox thrice.lox
expect "1
2
3"


spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox varparam.lox
expect "\[line 2\] Error at 'a': Variable with this name already declared in this scope.
\[line 1\] Error at 'a': Local variable never used.
\[line 2\] Error at 'a': Local variable never used."


spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox timeit.lox
expect -re {[0-9]\.[0-9]+}


#interact

