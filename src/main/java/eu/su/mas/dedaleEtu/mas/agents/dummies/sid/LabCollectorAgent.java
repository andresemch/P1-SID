package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.EnvironmentType;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class LabCollectorAgent extends AbstractDedaleAgent {
    /**
     * This method is automatically called when "agent".start() is executed.
     * Consider that Agent is launched for the first time.
     * 1) set the agent attributes
     * 2) add the behaviours
     */

    Couple<Couple<String,String>, SerializableSimpleGraph<String, MapRepresentation.MapAttribute>> struct;
    gsLocation originA;
    gsLocation destino;
    gsLocation finExplo;
    MapRepresentation myMap;

    List<String> path;

    SerializableSimpleGraph<String, MapRepresentation.MapAttribute> returnMap = new SerializableSimpleGraph<>();

    boolean found = false;
    AID ExplorerAID;



    protected void setup() {
        super.setup();
      //  myMap = new MapRepresentation();

        List<Behaviour> lb = behavioursList();

        addBehaviour(new startMyBehaviours(this, lb));
    }

    private List<Behaviour> behavioursList() {
        //use them as parameters for your behaviours is you want
        List<Behaviour> lb = new ArrayList<>();
        // ADD the initial behaviours
        lb.add(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    register();
                    originA = (gsLocation) getCurrentPosition();
                    System.out.println("Soy el Agente B y mi AID es " + getAID().getName());
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("LabCollector (Agente B) registered");
            }
        });
        lb.add(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    searchAgent();
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        lb.add(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        struct = (Couple<Couple<String,String>, SerializableSimpleGraph<String, MapRepresentation.MapAttribute>>) msg.getContentObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("REACHES HERE");
                    System.out.println("Ubicacio " + struct.getLeft());
                    destino= new gsLocation(struct.getLeft().getLeft());
                    finExplo= new gsLocation(struct.getLeft().getRight());
                    returnMap = struct.getRight();
                    System.out.println(returnMap);
                    if (myMap!=null) myMap.mergeMap(returnMap);
                    else {
                        MapRepresentation mapp= new MapRepresentation();
                        mapp.mergeMap(returnMap);
                        myMap=mapp;
                    }
                    myMap.addNode(getCurrentPosition().toString(), MapRepresentation.MapAttribute.closed);

                    for (Couple<Location, List<Couple<Observation, Integer>>> obs: observe()) {

                        myMap.addNode(obs.getLeft().toString(), MapRepresentation.MapAttribute.open);
                        myMap.addEdge(getCurrentPosition().toString(), obs.getLeft().toString());
                    }
                    myMap.removeNode(finExplo.toString());
                    System.out.println("\n origin A es: "+ originA.toString() + " " + originA.toString().getClass());
                    System.out.println("\n destino es:  "+ destino.toString() + " " + destino.toString().getClass());

                 //   path = myMap.getShortestPath(originA.toString(), destino.toString());
                    found = true;
                    //System.out.print("\n MAP RECEIVED \n" );

                    System.out.println("Col: Llego el mensaje");
                    System.out.println("\n open nodes:"+myMap.getOpenNodes());
                    sendingMessage();
                }
            }
        });
        /*lb.add(new CyclicBehaviour() {
            @Override
            public void action() {
                if (found) {
                    System.out.println("\n -- PATH: --"+path+"\n");
                    for (Couple<Location, List<Couple<Observation, Integer>>> obs: observe()) {
                        System.out.println("\n ** neighbour: **"+obs.getLeft().toString());
                    }
                    System.out.println("\n");

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (path.isEmpty()) done();
                    else {
                        //Location nextMove =
                        String nextMove= path.get(0);
                        if (nextMove!= finExplo.toString()) moveTo(new gsLocation(path.get(0)));
                        if (getCurrentPosition() == destino) done();
                        else path.remove(0);
                    }
                }
            }
        });*/
        lb.add(new SimpleBehaviour() {

            private boolean finished = false;
            @Override
            public void action() {
                if (found && !getCurrentPosition().toString().equals(destino.toString())) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (getCurrentPosition().toString().equals(destino.toString()) || getCurrentPosition().toString().equals(finExplo.toString())) {
                        finished = true;
                        System.out.println("DEBERIA ESTAR FINISHED");
                    }
                    else {
                        //Location nextMove =
                        List<String> vecinos = new ArrayList<>();
                        for (Couple<Location, List<Couple<Observation, Integer>>> obs: observe()) {
                             vecinos.add(obs.getLeft().toString());
                        }
                        List<String> path = myMap.getShortestPath(getCurrentPosition().toString(), destino.toString());
                        String nextMove = null;
                        System.out.println("path: "+path+"\n");
                        if (path != null && !path.isEmpty()) {
                            nextMove = path.get(0);
                            if (nextMove!= finExplo.toString() && vecinos.contains(nextMove)) {
                                System.out.println("\n Next move is:"+nextMove+"\n");
                                moveTo(new gsLocation(nextMove));
                            }
                        }
                        else {
                            String aux = vecinos.get(0);

                            if (aux != finExplo.toString() ) {
                                moveTo(new gsLocation(vecinos.get(1)));
                            }
                            else moveTo(new gsLocation(aux));
                        }
                        if (getCurrentPosition().toString().equals(destino.toString())) finished = true;
                        //else path.remove(0);
                    }
                }
            }

            public boolean done() {
                return finished;
            }
        });

        return lb;
    }

    private void sendingMessage() {
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (ExplorerAID);
        msg.setContent(String.valueOf(originA));
        msg.setSender(this.getAID());
        sendMessage (msg); //IMPORTANTE PARA RESPETAR EL RANGO DE COMUNICACIÓN
    }

    private void searchAgent() throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("agentExplo");
        template.addServices(templateSd);

        SearchConstraints sc = new SearchConstraints();
        // We want to receive 10 results at most
        sc.setMaxResults(new Long(10));

        DFAgentDescription[] results = DFService.search(this, template, sc);

        if (results.length > 0) {
            DFAgentDescription dfd = results[0];
            ExplorerAID = dfd.getName();
            System.out.println("The explorer AID is " + ExplorerAID);
        }
    }
    private void register() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("LabCollector");
        sd.setType("agentCollect");
        dfd.addServices(sd);
        DFService.register(this,dfd);
    }

    /**
     * This method is automatically called after doDelete()
     */
    protected void takeDown() {
        super.takeDown();
    }

    /**
     * This method is automatically called before migration.
     * You can add here all the saving you need
     */
    protected void beforeMove() {
        super.beforeMove();
    }

    /**
     * This method is automatically called after migration to reload.
     * You can add here all the info regarding the state you want your agent to restart from
     */
    protected void afterMove() {
        super.afterMove();
    }
}
