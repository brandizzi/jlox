#!/usr/bin/expect -f

spawn -noecho java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox badfun.lox
expect {
    "\[line 3\] Error at 'a': Variable with this name already declared in this scope."
    "\[line 2\] Error at 'a': Local variable never used."
    "\[line 3\] Error at 'a': Local variable never used."
}

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox badret.lox
expect "\[line 1\] Error at 'return': Cannot return from top-level code."

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox checkuse.lox
expect "\[line 4\] Error at 'b': Local variable never used."

spawn java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox counter.lox
expect {
  "1" {}
  "2" {}
}

interact
