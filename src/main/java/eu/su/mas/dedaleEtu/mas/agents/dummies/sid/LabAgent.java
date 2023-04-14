package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SearchBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import net.sourceforge.plantuml.command.PSystemAbstractFactory;

import java.io.IOException;
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
    MapRepresentation myMap;

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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ACLMessage msg = receive();
                    if (msg != null) {
                        System.out.println("Exp: Responde correctamente");
                        found = true;
                    }
                }
            }
        });
        sBehaviour = new SearchBehaviour(this, myMap);
        lb.add(sBehaviour);
        return lb;
    }



    private void sendingMessage() throws IOException {
        System.out.printf("Exp: intento envio");
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (CollectorAID);
        if (sBehaviour != null) System.out.println("EL PUTO MAPAA " + sBehaviour.getMyMap());
        if (sBehaviour != null) myMap = sBehaviour.getMyMap();
        Couple<String, SerializableSimpleGraph<String, MapRepresentation.MapAttribute>> struct;
        if (myMap != null) {
            //myMap.prepareMigration();
            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg = myMap.getSerializableGraph();
            struct = new Couple<>(origin.toString(), sg);
        }
        else {
            //MapRepresentation emptyMap = new MapRepresentation();
            //emptyMap.prepareMigration();
            SerializableSimpleGraph<String, MapRepresentation.MapAttribute> sg = new SerializableSimpleGraph<>();
            struct = new Couple<>(origin.toString(), sg);
        }
        msg.setContentObject(struct);
        //msg.setContentObject(myMap);
        msg.setSender(this.getAID());
        sendMessage (msg); //IMPORTANTE PARA RESPETAR EL RANGO DE COMUNICACIÓN
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
