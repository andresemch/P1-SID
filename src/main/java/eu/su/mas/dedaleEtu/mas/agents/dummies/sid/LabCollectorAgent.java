package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import jade.core.AID;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class LabCollectorAgent extends AbstractDedaleAgent {
    /**
     * This method is automatically called when "agent".start() is executed.
     * Consider that Agent is launched for the first time.
     * 1) set the agent attributes
     * 2) add the behaviours
     */

    Location originA;
    boolean found = false;
    AID ExplorerAID;


    protected void setup() {
        super.setup();

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
                    System.out.println("Col: Llego el mensaje " + msg.getContent());
                    sendingMessage();
                }
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
