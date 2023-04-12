package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
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

    protected void setup() {
        super.setup();

        //AID and roles
        System.out.println("My AID is " + getAID().getName());
        Object[] args = getArguments();
        if (args != null) {
            for (int i=0; i < args.length; ++i) {
                System.out.println(args[i]+ " ");
            }
        }

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
                System.out.println("One shot behave");
                try {
                    register();
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }

                //Algoritmo de búsqueda de recursos
                String ubiActual = String.valueOf(getCurrentPosition());
                System.out.println("Mi ubicacion es " + getCurrentPosition().toString());

                System.out.println("Lab agent registered");
            }
        });
        lb.add(new CyclicBehaviour() {
            @Override
            public void action() {
                System.out.println("Lista observe " + observe());

                boolean moving = moveTo(observe().get(1).getLeft());
            }
        });
        lb.add(new CyclicBehaviour() {
            @Override
            public void action() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    AID aidAgent = searchAgent();
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Cyclic operation of send message");
                sendingMessage();
            }
        });
        /*lb.add(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                System.out.println("Another tick");
            }
        }); //tickerBehaviour*/
        /*SequentialBehaviour sb = new SequentialBehaviour();
        sb.add(new TickerBehaviour() {

            @Override
            public void action() {

            }

            @Override
            public boolean done() {
                return false;
            }
        });*/
        /*lb.add(new WakerBehaviour(this, 5000) {
            @Override
            protected void onWake() {
                System.out.println("I'm awake");
                super.onWake();
            }
        });*/
        //lb.add(new SayHelloBehaviour(this));
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

    private AID searchAgent() throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("agentCollect");
        template.addServices(templateSd);

        SearchConstraints sc = new SearchConstraints();
        // We want to receive 10 results at most
        sc.setMaxResults(new Long(10));

        DFAgentDescription[] results = DFService.search(this, template, sc);

        AID provider = null;
        if (results.length > 0) {
            DFAgentDescription dfd = results[0];
            provider = dfd.getName();
            System.out.println("The explorer AID is " + provider);
        }
        return provider;
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
