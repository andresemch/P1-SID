package eu.su.mas.dedaleEtu.mas.behaviours;

import com.sun.corba.ee.impl.encoding.BufferManagerWrite;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 8567689731496787661L;
    private boolean finished = false;

    /**
     * Current knowledge of the agent regarding the environment
     */
    private MapRepresentation myMap;

    /**
     * Nodes known but not yet visited
     */
    private final List<String> openNodes;
    /**
     * Visited nodes
     */
    private final Set<String> closedNodes;

    private final AID CollectorAID;

    private final Location origin;

    public SearchBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, AID CollectorAID, Location origin) {
        super(myagent);
        this.myMap = myMap;
        this.openNodes = new ArrayList<>();
        this.closedNodes = new HashSet<>();
        this.CollectorAID = CollectorAID;
        this.origin = origin;
    }

    @Override
    public void action() {
        if (this.myMap == null)
            this.myMap = new MapRepresentation();

        //0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();

        if (myPosition != null) {
            //List of observable from the agent's current position
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

            // Just added here to let you see what the agent is doing, otherwise he will be too quick
            try {
                this.myAgent.doWait(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //1) remove the current node from openlist and add it to closedNodes.
            this.closedNodes.add(myPosition);
            this.openNodes.remove(myPosition);

            this.myMap.addNode(myPosition, MapRepresentation.MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            String nextNode = null;
            for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft().getLocationId();
                if (!this.closedNodes.contains(nodeId)) {


                    boolean wind = false;
                    if (!this.openNodes.contains(nodeId)) {

                        for (Couple<Observation, Integer> ob : lob.getRight()) {
                            if (ob.getLeft() == Observation.WIND) {
                                System.out.println("WIND found");
                                wind = true;
                                break;
                            }
                        }
                        if (!wind) {
                            this.openNodes.add(nodeId);
                            this.myMap.addNode(nodeId, MapRepresentation.MapAttribute.open);
                            this.myMap.addEdge(myPosition, nodeId);
                        }
                        else this.closedNodes.add(nodeId);

                    } else {
                        //the node exist, but not necessarily the edge
                        this.myMap.addEdge(myPosition, nodeId);
                    }
                    if (nextNode == null && !wind) nextNode = nodeId;
                }
            }

            //3) while openNodes is not empty, continues.

            if (this.openNodes.isEmpty()) {
                //Explo finished
                finished = true;
                System.out.println("Exploration successufully done, behaviour removed.");
            } else {
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    finished = true;
                    System.out.println("Search behaviour finished");
                } else {


                    if (nextNode == null) {
                        //there is no directly accessible openNode
                        //chose one, compute the path and take the first step.
                        nextNode = this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
                    }

                    System.out.println(this.myAgent.getLocalName() + " - State of the observations : " + lobs);


                    // END API CALL ILUSTRATION
                    ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNode));
                }
            }
        }
    }

    @Override
    public boolean done() {
        return finished;
    }
}
