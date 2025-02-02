package Classes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dagsolver {
    List<Node> DAG;
    Boolean unsat_flag;
    Set<List<Integer>> equalities;
    Set<List<Integer>> inequalities;


    public Dagsolver(List<Node> DAG, Set<List<Integer>> equalities, Set<List<Integer>> inequalities) {
        this.DAG = DAG;
        this.unsat_flag = false;
        this.equalities = equalities;
        this.inequalities = inequalities;
    }

    public List<Node> getDAG() {
        return DAG;
    }

    public Set<List<Integer>> getEqualities() {
        return equalities;
    }

    public Set<List<Integer>> getInequalities() {
        return inequalities;
    }


    public Node NodefromId(int id) {
        for (Node n : DAG) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

    public int FindfromId(int id) {
        return NodefromId(id).getFind();
    }

    public Set<Integer> CcparfromId(int id) {
        return NodefromId(FindfromId(id)).getccpar();
    }

    public void Union(int id_1, int id_2){
        Node n1 = NodefromId(FindfromId(id_1));
        Node n2 = NodefromId(FindfromId(id_2));
        if (n1.forbidden_set.contains(n2.getFind()) || n2.forbidden_set.contains(n1.getFind())) {
            unsat_flag = true;
            return;
        }

        //heuristic on ccpar
        if(n1.getccpar().size() > n2.getccpar().size()) {
            //n1 is the new representative
            //System.out.println(n2.getFind());
            n1.setccpar(n2.getccpar());
            n2.resetccpar();
            n1.setForbidden_set(n2.getForbidden_set());
            n2.resetForbidden_set();
            for (Node n : DAG){
                if (n.getFind() == n2.getFind()) {
                    n.setFind(n1.getFind());
                }
            }
        } 
        else {
            //n2 is the new representative
            n2.setccpar(n1.getccpar());
            n1.resetccpar();
            n2.setForbidden_set(n1.getForbidden_set());
            n1.resetForbidden_set();
            for (Node n : DAG){
                if (n.getFind() == n1.getFind()) {
                    n.setFind(n2.getFind());
                }
            }
        }
    }

    public Boolean Congruent(int id_1, int id_2) {
        Node n1 = NodefromId(id_1);
        Node n2 = NodefromId(id_2);
        if (n1.getFn().equals(n2.getFn()) && n1.getArgs().size() == n2.getArgs().size()){
            for (int i = 0; i < n1.getArgs().size(); i++) {
                if (FindfromId(n1.getArgs().get(i)) != FindfromId(n2.getArgs().get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void Merge(int id_1, int id_2) {
        //System.out.println("Trying to merge " + id_1 + " and " + id_2);
        if (FindfromId(id_1) != FindfromId(id_2)) {
            Set<Integer> ccpar_1 = CcparfromId(id_1);
            Set<Integer> ccpar_2 = CcparfromId(id_2);
            Union(id_1, id_2);
            //union modify the ccpar of id_1 and id_2 
            //our CcparfromId is a copy of the ccpar of the node so they are the original
            for (int t1 : ccpar_1) {
                for(int t2 : ccpar_2) {
                    if (FindfromId(t1) != FindfromId(t2) && Congruent(t1,t2)) {
                        Merge(t1,t2);
                    }
                }
            }
        }
    }

    public Boolean solve () {
        for (List<Integer> pair : equalities) {
            if(unsat_flag) {
                return false;
            }
            Merge(pair.get(0),pair.get(1));
        }
        
        if(unsat_flag) {
            return false;
        }
        //check if all inequalities are in the same congruence class
        for (List<Integer> pair : inequalities) {
            if (FindfromId(pair.get(0)) == FindfromId(pair.get(1))) {
                unsat_flag = true;
                return false;
            }
            
        }

        //we check if some literal that is argument of atom is in same congruence class with a cons
        //this save all the arguments of atom
        Set<Integer> is_atom = new HashSet<>();
        for (Node n : DAG) {
            Set<Integer> ccpar = CcparfromId(n.getId());
            for (Integer parent : ccpar){
                if(NodefromId(parent).getFn().equals("atom")) {
                    //i save the find of the atoms
                    is_atom.add(n.getFind());
                }
            }
        }
        //this check if some arguments of atom are in the same congruence class with a cons
        for(Node n : DAG) {
            if (is_atom.contains(n.getFind()) && n.getFn().equals("cons")) {
                unsat_flag = true;
                return false;
            }
        }
        //this check is needed if we don't have any equalities select (a,j) != select (a,j)
        return true;
    }

    @Override
    public String toString(){
        //if(unsat_flag) {
        //    return "UNSAT\n";
        //}
        return "DAG = \n " + DAG + "}\n\n";
    }
}
