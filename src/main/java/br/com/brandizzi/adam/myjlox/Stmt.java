package br.com.brandizzi.adam.myjlox;

import java.util.Collections;
import java.util.List;

import br.com.brandizzi.adam.myjlox.Stmt.Function;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitBlockStmt(Block stmt);
    R visitClassStmt(Class stmt);
    R visitReturnStmt(Return stmt);
    R visitFunctionStmt(Function stmt);
    R visitVarStmt(Var stmt);
    R visitIfStmt(If stmt);
    R visitWhileStmt(While stmt);
    R visitBreakStmt(Break stmt);
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Class extends Stmt {
    Class(Token name, List<Stmt.Function> methods) {
      this(name, methods, Collections.<Function>emptyList());
    }

    public Class(
        Token name, List<Function> methods, List<Function> classMethods
    ) {
        this(name, methods,classMethods, Collections.<Function>emptyList());
    }

    public Class(
        Token name, List<Function> methods, List<Function> classMethods,             
        List<Function> getters
    ) {
        this.name = name;
        this.methods = methods;
        this.classMethods = classMethods;
        this.getters = getters;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final List<Function> getters;
    final List<Function> classMethods;
    final Token name;
    final List<Stmt.Function> methods;
  }
  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }
  static class Function extends Stmt {
    Function(Token name, List<Token> parameters, List<Stmt> body) {
      this.name = name;
      this.parameters = parameters;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> parameters;
    final List<Stmt> body;
  }
  static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }
  static class Break extends Stmt {
    Break(Token command) {
      this.command = command;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

    final Token command;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
