import java.util.*;
import java.io.*;
import java.math.*;
public class Main{
    static BufferedReader infile;
    static StringTokenizer st;
     public static void main(String []args) throws Exception
     {  
        boolean verbose = false;
        boolean alphaBetaPruning = false;
        boolean maxRoot =false;
        String filePath="";

        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-v")) verbose =true;
            else if(args[i].equals("-ab")) alphaBetaPruning =true;
            else if(args[i].equals("max")) maxRoot=true;
            else if(args[i].endsWith(".txt")) filePath=args[i];
        }

		solve(verbose,alphaBetaPruning,maxRoot,filePath);
     }
     public static void solve(boolean verbose, boolean alphaBetaPruning, boolean maxRoot, String filePath) throws Exception{
        
        if (filePath.equals("")) {
            System.out.println("Missing required input file name");
            return;
        }

        Map<String,List<String>> edge=new HashMap<>(); // store the mapping of internal nodes to its child nodes.
        Map<String, Integer> value = new HashMap<>(); // store the mapping of leaf node to its value.
        try{
            File inputFile = new File("./"+filePath);
            Scanner scanner = new Scanner(inputFile);  
            while(scanner.hasNext()){
                String inputLine = scanner.nextLine();  
                if(inputLine.contains("=")){   // leaf node
                    String[] split_inputLine = inputLine.split("=");
                    String leaf_Node = split_inputLine[0].trim();
                    int val = Integer.valueOf(split_inputLine[1].trim());
                    value.put(leaf_Node,val);
                }
                else{ // internal node
                    String[] split_inputLine = inputLine.split(":");
                    String internal_Node = split_inputLine[0].trim();
                    String child = split_inputLine[1].substring(split_inputLine[1].indexOf("[")+1, split_inputLine[1].indexOf("]"));
                    String[] child_list=child.split(",");
                    edge.put(internal_Node,new ArrayList<>());
                    for(int i=0;i<child_list.length;i++){
                        child_list[i]=child_list[i].trim();
                        edge.get(internal_Node).add(child_list[i]);
                    }
                }
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        for(String vertex: edge.keySet()) Collections.sort(edge.get(vertex));

        // Initial values of Alpha and Beta
        int alpha = Integer.MIN_VALUE; int beta = Integer.MAX_VALUE;

        String rootNode =  findRootNode(edge); // the function to find root node of the graph
        if(rootNode.equals("multiple roots")) return; 

        List<String> missingNode = checkMissingNode(edge, value);// the function to check whether there is missing node in graph
        if(missingNode.size()>0){
            for(String error: missingNode) System.out.println(error);
            return;
        }

        String currentNode = rootNode;
        if(alphaBetaPruning) minimax_ab(currentNode, maxRoot,edge,value,alpha,beta, verbose, rootNode); 
        else minimax(currentNode, maxRoot,edge,value, verbose, rootNode); 
     }
     
    
    // Returns optimal value for current player
    public static int minimax_ab(String currentNode, boolean maximizingPlayer, Map<String,List<String>> edge, Map<String, Integer> value, int alpha,int beta, boolean verbose, String rootNode){
        // Terminating condition. i.e leaf node is reached
        if (!edge.containsKey(currentNode)) return value.get(currentNode);
        if (maximizingPlayer){
            int best = Integer.MIN_VALUE;
            String bestChild = "";
            boolean prune = false;
            // call recursive function for its children
            for (String child: edge.get(currentNode)){
                int val = minimax_ab(child, false, edge,value, alpha, beta, verbose, rootNode);
                if(val>best){
                    best=val;
                    bestChild=child;
                }
                alpha = Math.max(alpha, val);
                // Alpha Beta Pruning
                if (beta <= alpha){
                    prune = true;
                    break;
                }
            }
            if(!prune){
                if(verbose) System.out.println("max("+currentNode+") chooses "+bestChild +" for "+best);
                else{
                    if(currentNode.equals(rootNode)) System.out.println("max("+currentNode+") chooses "+bestChild +" for "+best);
                }
            }
            return best;
        }

        else{
            int best = Integer.MAX_VALUE;
            String bestChild = "";
            boolean prune = false;
            // call recursive function for its children
            for (String child: edge.get(currentNode)){
                int val = minimax_ab(child,true,edge,value,alpha,beta, verbose, rootNode);
                if(val<best){
                    best=val;
                    bestChild=child;
                }
                beta = Math.min(beta, val);
                // Alpha Beta Pruning
                if (beta <= alpha){
                    prune=true;
                    break;
                }
            }
            if(!prune){
                if(verbose) System.out.println("min("+currentNode+") chooses "+bestChild +" for "+best);
                else{
                    if(currentNode.equals(rootNode)) System.out.println("min("+currentNode+") chooses "+bestChild +" for "+best);
                }
            }
            return best;
        }
    }
    
    // Returns optimal value for current player
    public static int minimax(String currentNode, boolean maximizingPlayer, Map<String,List<String>> edge, Map<String, Integer> value, boolean verbose, String rootNode){
        // Terminating condition. Leaf node is reached
        if (!edge.containsKey(currentNode)) return value.get(currentNode);
        if (maximizingPlayer){
            int best = Integer.MIN_VALUE;
            String bestChild = "";
            // call recursive function for its children
            for (String child: edge.get(currentNode)){
                int val = minimax(child, false, edge, value, verbose, rootNode);
                if(val>best){
                    best=val;
                    bestChild=child;
                }
            }
            
            if(verbose) System.out.println("max("+currentNode+") chooses "+bestChild +" for "+best);
            else{
                if(currentNode.equals(rootNode)) System.out.println("max("+currentNode+") chooses "+bestChild +" for "+best);
            }
            return best;
        }

        else{
            int best = Integer.MAX_VALUE;
            String bestChild = "";
            // call recursive function for its children
            for (String child: edge.get(currentNode)){
                int val = minimax(child, true, edge, value, verbose, rootNode);
                if(val<best){
                    best=val;
                    bestChild=child;
                }
            }
            
            if(verbose) System.out.println("min("+currentNode+") chooses "+bestChild +" for "+best);
            else{
                if(currentNode.equals(rootNode)) System.out.println("min("+currentNode+") chooses "+bestChild +" for "+best);
            }
            return best;
        }
    }
    
    public static String findRootNode(Map<String,List<String>> edge){
        Map<String,Integer> indegree = new HashMap<>();// store the mapping of internal node to its indegree value.
        for(String internalNode: edge.keySet()) indegree.put(internalNode,0);
        for(String internalNode:edge.keySet()){
            List<String> child_list = edge.get(internalNode);
            for(String child: child_list){
                if(!indegree.containsKey(child)) continue; // leaf node
                indegree.put(child, indegree.get(child)+1);
            }
        }
        List<String> root_list = new ArrayList<>(); // store the node with indegree value equal to zero. i.e. root node.
        for(String internalNode:indegree.keySet()){
            if(indegree.get(internalNode)==0) root_list.add(internalNode);
        }
        if(root_list.size()==1) return root_list.get(0); // valid graph, with only one root node
        else{  // invalid graph, with more than one root note.
            System.out.print("multiple roots: ");
            int size = root_list.size();
            for(int i=0;i<size;i++){
                if(i!=size-1) System.out.print("\""+root_list.get(i)+"\"" + " and ");
                else System.out.print("\""+root_list.get(i)+"\"");
            }
            System.out.println();
            return ("multiple roots");
        } 
    }
    public static List<String> checkMissingNode(Map<String,List<String>> edge, Map<String,Integer> value){
        List<String> missingNode = new ArrayList<>();
        for(String internalNode: edge.keySet()){
            List<String> child_list = edge.get(internalNode);
            for(String child: child_list){
                if(!edge.containsKey(child) && !value.containsKey(child)){
                    String error = "child node \""+ child + "\" of \""+ internalNode + "\" not found";
                    missingNode.add(error);
                }
            }
        }
        return missingNode;
    }
}
