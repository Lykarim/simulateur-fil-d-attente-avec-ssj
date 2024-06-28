package tp_home;



import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class MMSQueueWithAbandon {

    RandomVariateGen genArr;
    RandomVariateGen genServ;
    RandomVariateGen genAbandon;
    LinkedList<Customer> waitList = new LinkedList<>();
    LinkedList<Customer> servList = new LinkedList<>();
    Tally custWaits = new Tally("Waiting times");
    Accumulate totWait = new Accumulate("Size of queue");
    int s; // Nombre de serveurs

    static class Customer {
        double arrivTime, servTime, abandonTime;
    }

    public MMSQueueWithAbandon(double lambda, double mu, double theta, int s) {
        genArr = new ExponentialGen(new MRG32k3a(), lambda);
        genServ = new ExponentialGen(new MRG32k3a(), mu);
        genAbandon = new ExponentialGen(new MRG32k3a(), theta);
        this.s = s;
    }

    public void simulate(double timeHorizon) {
        Sim.init();
        new EndOfSim().schedule(timeHorizon);
        new Arrival().schedule(genArr.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {
        public void actions() {
            new Arrival().schedule(genArr.nextDouble()); // Prochaine arrivée
            Customer cust = new Customer(); // Nouveau client
            cust.arrivTime = Sim.time();
            cust.servTime = genServ.nextDouble();
            cust.abandonTime =  genAbandon.nextDouble();
            if (servList.size() < s) { // Démarre le service si un serveur est disponible
                custWaits.add(0.0);
                servList.addLast(cust);
                new Departure(cust).schedule(cust.servTime);
            } else { // Rejoint la file d'attente
                waitList.addLast(cust);
                totWait.update(waitList.size());
                new Abandonment(cust).schedule(cust.abandonTime);
            }
        }
    }

    class Departure extends Event {
        Customer cust;

        Departure(Customer cust) {
            this.cust = cust;
        }

        public void actions() {
            servList.remove(cust);
            if (!waitList.isEmpty()) { // Démarre le service pour le prochain client dans la file
                Customer nextCust = waitList.removeFirst();
                totWait.update(waitList.size());
                custWaits.add(Sim.time() - nextCust.arrivTime);
                servList.addLast(nextCust);
                new Departure(nextCust).schedule(nextCust.servTime);
            }
        }
    }

    class Abandonment extends Event {
        Customer cust;

        Abandonment(Customer cust) {
            this.cust = cust;
        }

        public void actions() {
            if (waitList.contains(cust)) {
                waitList.remove(cust);
                totWait.update(waitList.size());
            }
        }
    }

    static class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

    public static void main(String[] args) {
        double mu = 2.0; // Taux de service
        double lambda = 1.0; // Taux d'arrivée
        double theta = 0.5; // Taux d'abandon
        int s = 1; // Nombre de serveurs
        MMSQueueWithAbandon queue = new MMSQueueWithAbandon(lambda, mu, theta, s);
        queue.simulate(100000.0);
        System.out.println(queue.custWaits.report());
        System.out.println(queue.totWait.report());
    }
}
