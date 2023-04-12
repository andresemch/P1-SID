package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SearchBehaviour;
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
                    System.out.println("Soy el Agente A y mi AID es " + getAID().getName());
                    register();
                    origin = getCurrentPosition();
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
        /*lb.add(new CyclicBehaviour() {
            @Override
            public void action() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Lista observe " + observe());
            }
        });*/
        lb.add(new SearchBehaviour(this, null));
        return lb;
    }



    private void sendingMessage() {
        ACLMessage msg = new ACLMessage (ACLMessage.INFORM);
        msg.addReceiver (new AID ("LabCollector", AID.ISLOCALNAME));
        msg.setContent("BUENAS"); send (msg);
    }

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
            System.out.println("The explorer AID is " + CollectorAID);
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
