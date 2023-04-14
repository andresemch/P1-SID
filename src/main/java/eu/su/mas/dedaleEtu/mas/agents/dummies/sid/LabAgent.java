package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import com.fasterxml.jackson.databind.ObjectMapper;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.SearchBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class LabAgent extends AbstractDedaleAgent {
    /**
     * This method is automatically called when "agent".start() is executed.
     * Consider that Agent is launched for the first time.
     * 1) set the agent attributes
     * 2) add the behaviours
     */

    Location origin;
    public MapRepresentation myMap = null;

    boolean found = false;

    AID CollectorAID;

    SearchBehaviour sBehaviour = null;

    protected void setup() {
        super.setup();

        //use them as parameters for your behaviours is you want
        List<Behaviour> lb = behavioursList();
        // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
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
                    origin = getCurrentPosition();
                    System.out.println("Soy el Agente A y mi AID es " + getAID().getName() + " y mi posición inicial es " + origin);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Lab agent (Agente A) registered");
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
                if (!found) {
                    try {
                        sendingMessage();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    ACLMessage msg = receive();
                    if (msg != null) {
                        System.out.println("Exp: Responde correctamente");
                        String move = msg.getContent();
                        //System.out.println("Posicion move final " + move);
                        //try {
                        //    Thread.sleep(6000);
                        //} catch (InterruptedException e) {
                        //    throw new RuntimeException(e);
                        //}
                        //moveTo(new gsLocation(move));
                        found = true;
                    }
                }
            }
        });
        sBehaviour = new SearchBehaviour(this, myMap);
        lb.add(sBehaviour);
        return lb;
    }



    private void sendingMessage() throws Exception {
        //System.out.printf("Exp: intento envio");
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (CollectorAID);
        if (sBehaviour != null) myMap = sBehaviour.getMyMap();
        Couple<Couple<String,String>, SerializableSimpleGraph<String, MapRepresentation.MapAttribute>> struct;

        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> grafo;
        if (myMap != null) {
            grafo = myMap.getSerializableGraph();
        }
        else {
            MapRepresentation emptyMap = new MapRepresentation();
            grafo=emptyMap.getSerializableGraph();
        }
        Couple<String,String> localizaciones= new Couple<>(origin.toString(),getCurrentPosition().toString());
        struct = new Couple<>(localizaciones, grafo);
        msg.setContentObject(struct);
        msg.setSender(this.getAID());
        sendMessage (msg); //IMPORTANTE PARA RESPETAR EL RANGO DE COMUNICACIÓN
    }

    public static String serialize(MapRepresentation map) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

    /*private void sendingMessage2() throws IOException {
        System.out.printf("Exp: intento envio");
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (CollectorAID);
        msg.setContentObject(myMap);
        msg.setSender(this.getAID());
        sendMessage (msg); //IMPORTANTE PARA RESPETAR EL RANGO DE COMUNICACIÓN
    }*/

    private void register() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("Lab");
        sd.setType("agentExplo");
        dfd.addServices(sd);
        DFService.register(this,dfd);
    }

    private void searchAgent() throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("agentCollect");
        template.addServices(templateSd);

        SearchConstraints sc = new SearchConstraints();
        // We want to receive 10 results at most
        sc.setMaxResults(new Long(10));

        DFAgentDescription[] results = DFService.search(this, template, sc);

        if (results.length > 0) {
            DFAgentDescription dfd = results[0];
            CollectorAID = dfd.getName();
            System.out.println("The collector AID is " + CollectorAID);
        }
    }

    /**
     * This method is automatically called after doDelete()
     */
    protected void takeDown() {
        System.out.println("I'm leaving. Good bye!");
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
