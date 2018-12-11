import java.util.ArrayList;

public class Codegen {

    public static int counter=0;

    public static String newLabel(){
        counter++;
        return "label"+counter;
    }

    public ArrayList<String> codegenExp(Exp e) throws basic.CodegenException {

        ArrayList<String> out_list= new ArrayList<>();
        if (e instanceof IntLiteral){
            out_list.add("li $a0 " + Integer.toString(((IntLiteral) e).n));
            return out_list;
        }

        else if (e instanceof Variable){
            int offset=4*((Variable) e).x;
            out_list.add ("lw $a0 " + Integer.toString(offset)+"$fp");
            return out_list;
        }

        else if (e instanceof If){
            String elseBranch= newLabel();
            String thenBranch= newLabel();
            String exitLabel= newLabel();

            out_list.addAll(codegenExp(((If) e).l));
            out_list.add ("sw $a0 0($sp)");
            out_list.add("addiu $sp $sp -4");

            out_list.addAll(codegenExp(((If) e).r));
            out_list.add("lw $t1 4($sp)");
            out_list.add("addiu $sp $sp 4");
            out_list.add("beq $a0 $t1 "+ thenBranch);

            out_list.add(elseBranch+":");
            out_list.addAll(codegenExp(((If) e).elseBody));
            out_list.add("b " + exitLabel);

            out_list.add(thenBranch+":");
            out_list.addAll(codegenExp(((If) e).thenBody));
            out_list.add(exitLabel+":");

            return out_list;
        }

        else if (e instanceof Binexp){
            out_list.addAll(codegenExp(((Binexp) e).l));
            out_list.add ("sw $a0 0($sp)");
            out_list.add("addiu $sp $sp -4");
            out_list.addAll(codegenExp(((Binexp) e).r));
            Binop casting=(((Binexp)e).binop);

            if(casting instanceof Plus){
                out_list.add("lw t1 4($sp)");
                out_list.add("add $a0 $t1 $a0");
                out_list.add("addiu $sp $sp 4");
            }

            else if(casting instanceof Minus){
                out_list.add("lw t1 4($sp)");
                out_list.add("sub $a0 $t1 $a0");
                out_list.add("addiu $sp $sp 4");
            }
            return out_list;
        }
        /*
        else if (e instanceof Comp.Equals){
            genExp(e.l);
            String out_string = new String("sw $a0 0($sp)\naddiu $sp $sp -4\n");
            genExp(e.r);
            out_string=out_string+("lw t1 4($sp)\nsub $a0 $t1 $a0\naddiu $sp $sp 4\n");
            return out_string;
        }*/
        else if (e instanceof Invoke){
            out_list.add("sw $fp 0($sp)");
            out_list.add("addiu $sp $sp -4");
            while (((Invoke) e).args.size()>0){
                out_list.addAll(codegenExp(((Invoke) e).args.remove(((Invoke) e).args.size()-1)));
                out_list.add("sw $a0 0($sp)");
                out_list.add("addiu $sp $sp -4");
            }
            out_list.add("jal "+((Invoke) e).name+"_entry");
            return out_list;
        }
        return out_list;
    }
    public ArrayList<String> codegenDecl(Declaration d) throws basic.CodegenException {
        ArrayList<String> out_list2= new ArrayList<>();
        int sizeAr = (2 + d.numOfArgs)*4;
        out_list2.add(d.id+"entry:");
        out_list2.add("move $fp $sp");
        out_list2.add("sw $ra 0($sp)");
        out_list2.add("addiu $sp $sp -4");
        out_list2.addAll(codegenExp(d.body));
        out_list2.add("lw $ra 4($sp)");
        out_list2.add("addiu $sp $sp "+sizeAr);
        out_list2.add("lw $fp 0($sp)");
        out_list2.add("jr $ra");
        return out_list2;
    }
    public String codegenProg(Program p){
        return null;
    }
}
