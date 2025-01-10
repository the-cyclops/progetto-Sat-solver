package Classes;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Node {
    int id;
    String fn;
    List<Integer> args;
    int find;
    Set<Integer> ccpar;
    Set<Integer> forbidden_set;

    public Node(int id, String fn, List<Integer> args, int find, Set<Integer> ccpar, Set<Integer> forbidden_set ) {
        this.id = id;
        this.fn = fn;
        this.args = args;
        this.find = find;
        this.ccpar = ccpar;
        this.forbidden_set = forbidden_set;
    }
    
    //constructor for costant symblos and variables
    public Node(int id, String s) {
        this.id = id;
        this.fn = s;
        this.args = null;
        this.find = id;
        this.ccpar = new HashSet<>();
        this.forbidden_set = new HashSet<>();
    }

    //constructor for function symbols
    public Node(int id, String fn, List<Integer> args) {
        this.id = id;
        this.fn = fn;
        this.args = args;
        this.find = id;
        this.ccpar = new HashSet<>();
        this.forbidden_set = new HashSet<>();
    }

    @Override
    public String toString(){
        return "Node={ \n"+ 
        "id = " + id + 
        "\n fn = " + fn +
        "\n args = " + args +
        "\n find = " + find +
        "\n ccpar = " + ccpar +
        "\n forbidden_set = " + forbidden_set + "}\n\n";
    }

    public int getId() {
        return id;
    }

    public String getFn() {
        return fn;
    }

    public List<Integer> getArgs() {
        return args;
    }

    public int getFind() {
        return find;
    }
    
    public void setFind(int find) {
        this.find = find;
    }

    public void setccpar(int ccpar) {
        this.ccpar.add(ccpar);
    }

    public void setccpar(Set<Integer> ccpar) {
        this.ccpar.addAll(ccpar);
    }

    public void resetccpar() {
        this.ccpar = new HashSet<>();
    }

    public Set<Integer> getccpar() {
        //return a copy of the set in order to merge correctly in dagsolver
        Set<Integer> to_return = new HashSet<>(ccpar);
        return to_return;
    }

    public void setForbidden_set(int forbidden) {
        this.forbidden_set.add(forbidden);
    }

    public void setForbidden_set(Set<Integer> forbidden_set) {
        this.forbidden_set.addAll(forbidden_set);
    }

    public Set<Integer> getForbidden_set() {
        return forbidden_set;
    }

    public void resetForbidden_set() {
        this.forbidden_set = new HashSet<>();
    }
}
