#!/usr/bin/env bash
FAIL=0
for l in examples/*.lox
do
    out=$(mktemp)
    err=$(mktemp)
    if [ -e $l.skip ]
    then
       echo SKIP $l
       continue
    elif [ ! -e $l.out ] || [ ! -e $l.err ]
    then
       echo missing $l.out or $l.err
       exit 1
    fi
    java -classpath target/classes/ br.com.brandizzi.adam.myjlox.Lox $l > $out 2> $err
    if ! diff $l.out $out ||  ! diff $l.err $err
    then
       $((FAIL++))
    fi

    if [ "$FAIL" != "0" ]
    then
       echo FAIL $l
    else
       echo PASS $l
    fi
done

if [ "$FAIL" != "0" ]
then
    echo "$FAIL failures."
fi
