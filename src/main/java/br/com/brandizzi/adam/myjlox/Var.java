package br.com.brandizzi.adam.myjlox;

abstract class Var extends Expr {
    public int index;
    public Token name;
}