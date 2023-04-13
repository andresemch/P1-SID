package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.SearchBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

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
    MapRepresentation myMap = null;

    boolean found = false;

    AID CollectorAID;

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
      Behaviour enviarmensaje= new CyclicBehaviour() {
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
        };
        lb.add(enviarmensaje);
        if (enviarmensaje.done()) System.out.println("\n***** Si que acaba el cyclic ******\n");

        Behaviour busca= new SearchBehaviour(this, myMap, CollectorAID, origin);
        lb.add(busca);
        if (busca.done()){
            System.out.println("\n ----Ha acabado el behaviour ---- \n");
            System.out.println(myMap==null);
        }
        return lb;
    }



    private void sendingMessage() throws IOException {
        System.out.printf("Exp: intento envio");
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (CollectorAID);
        Couple<String, MapRepresentation> struct = new Couple<>(origin.toString(), myMap);
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
