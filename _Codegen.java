import java.util.ArrayList;
import java.util.List;

public class _Codegen {


    public static List<Declaration> mainList;
    public static int counter=0;

    public static String newLabel()
    {
        counter++;
        return "label"+counter;
    }

    public _Codegen(){}


    public ArrayList<String> codegenDecl(Declaration d)
    {
        ArrayList<String> out_list2= new ArrayList<>();
        int sizeAr = (2 + d.numOfArgs)*4;

        out_list2.add(d.id+"_entry:");
        out_list2.add("move $fp $sp");
        out_list2.add("sw $ra 0($sp)");
        out_list2.add("addiu $sp $sp -4");

        out_list2.addAll(codegenExp(d.body));

        out_list2.add("lw $ra 4($sp)");
        out_list2.add("addiu $sp $sp "+sizeAr);
        out_list2.add("lw $fp 0($sp)");

        if(mainList.indexOf(d)==0)
        {
            out_list2.add("li $v0 10");
            out_list2.add("syscall");
        }
        else 
        {
            out_list2.add("jr $ra");
        }

        return out_list2;
    }

    public String codegenProg(Program p)
    {
        mainList=p.decls;
        ArrayList<String> stringList = new ArrayList<>();
        String newLine="\n";
        String output="";

        for ( Declaration decs : mainList)
        {
            stringList.addAll(codegenDecl(decs));
        }

        for(String tokens:stringList)
        {
            output+=tokens+newLine;
        }
        return output;
    }

    public ArrayList<String> codegenExp(Exp e)
    {
        ArrayList<String> out_list= new ArrayList<>();      // E -> INT
        if (e instanceof IntLiteral)
        {
            out_list.add("li $a0 " + Integer.toString(((IntLiteral) e).n));
            return out_list;
        }

        else if (e instanceof Variable)      // E -> ID
        {
            int offset=4*((Variable) e).x;
            out_list.add("lw $a0 " + Integer.toString(offset)+"($fp)");
            return out_list;
        }

        else if (e instanceof If)      // E -> if E COMP E then E else E endif
        {
            String elseBranch= newLabel();
            String thenBranch= newLabel();
            String exitLabel= newLabel();

            out_list.addAll(codegenExp(((If) e).l));
            out_list.add("sw $a0 0($sp)");
            out_list.add("addiu $sp $sp -4");

            out_list.addAll(codegenExp(((If) e).r));
            out_list.add("lw $t1 4($sp)");
            out_list.add("addiu $sp $sp 4");

            if(((If) e).comp instanceof Equals)
            {
                out_list.add("beq $a0 $t1 "+ thenBranch);
            }

            else if(((If) e).comp instanceof Less)
            {
                // do the comparison here
            }

            out_list.add(elseBranch+":");
            out_list.addAll(codegenExp(((If) e).elseBody));
            out_list.add("b " + exitLabel);

            out_list.add(thenBranch+":");
            out_list.addAll(codegenExp(((If) e).thenBody));
            out_list.add(exitLabel+":");

            return out_list;
        }

        else if (e instanceof Binexp)      // E -> (E BINOP E)
        {
            out_list.addAll(codegenExp(((Binexp) e).l));
            out_list.add("sw $a0 0($sp)");
            out_list.add("addiu $sp $sp -4");
            out_list.addAll(codegenExp(((Binexp) e).r));
            Binop casting=(((Binexp)e).binop);

            if(casting instanceof Plus)
            {
                out_list.add("lw $t1 4($sp)");
                out_list.add("add $a0 $t1 $a0");
            }

            else if(casting instanceof Minus)
            {
                out_list.add("lw $t1 4($sp)");
                out_list.add("sub $a0 $t1 $a0");
            }

            out_list.add("addiu $sp $sp 4");
            return out_list;
        }

        else if (e instanceof Invoke)      // E -> ID(ARGS)
        {
            List<Exp> newList = ((Invoke)e).args;
            out_list.add("sw $fp 0($sp)");
            out_list.add("addiu $sp $sp -4");
            int iterator=newList.size()-1;
            while(iterator >= 0)
            {
                Exp toAdd=newList.get(iterator);
                out_list.addAll(codegenExp(toAdd));
                out_list.add("sw $a0 0($sp)");
                out_list.add("addiu $sp $sp -4");
                iterator--;
            }
            out_list.add("jal "+((Invoke) e).name+"_entry");
            return out_list;
        }

        else if (e instanceof Skip)
        {
            out_list.add("sll $zero $zero 0");
            return out_list;
        }

        return out_list;
    }



}
