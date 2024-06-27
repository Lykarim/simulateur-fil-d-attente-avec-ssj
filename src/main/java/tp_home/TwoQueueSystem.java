package tp_home;



import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class TwoQueueSystem {

    RandomVariateGen genArr1;
    RandomVariateGen genArr2;
    RandomVariateGen genServ;
    LinkedList<Customer> waitList1 = new LinkedList<>();
    LinkedList<Customer> waitList2 = new LinkedList<>();
    LinkedList<Customer> servList = new LinkedList<>();
    Tally custWaits1 = new Tally("Waiting times for Queue 1");
    Tally custWaits2 = new Tally("Waiting times for Queue 2");
    Accumulate totWait1 = new Accumulate("Size of queue 1");
    Accumulate totWait2 = new Accumulate("Size of queue 2");
    double probQueue1; // Probabilité de servir un client de la file 1

    class Customer {
        double arrivTime, servTime;
    }

    public TwoQueueSystem(double lambda1, double lambda2, double mu, double probQueue1) {
        genArr1 = new ExponentialGen(new MRG32k3a(), lambda1);
        genArr2 = new ExponentialGen(new MRG32k3a(), lambda2);
        genServ = new ExponentialGen(new MRG32k3a(), mu);
        this.probQueue1 = probQueue1;
    }

    public void simulate(double timeHorizon) {
        Sim.init();
        new EndOfSim().schedule(timeHorizon);
        new Arrival1().schedule(genArr1.nextDouble());
        new Arrival2().schedule(genArr2.nextDouble());
        Sim.start();
    }

    class Arrival1 extends Event {
        public void actions() {
            new Arrival1().schedule(genArr1.nextDouble()); // Prochaine arrivée pour la file 1
            Customer cust = new Customer();
            cust.arrivTime = Sim.time();
            cust.servTime = genServ.nextDouble();
            if (servList.isEmpty()) {
                custWaits1.add(0.0);
                servList.addLast(cust);
                new Departure(cust, 1).schedule(cust.servTime);
            } else {
                waitList1.addLast(cust);
                totWait1.update(waitList1.size());
            }
        }
    }

    class Arrival2 extends Event {
        public void actions() {
            new Arrival2().schedule(genArr2.nextDouble()); // Prochaine arrivée pour la file 2
            Customer cust = new Customer();
            cust.arrivTime = Sim.time();
            cust.servTime = genServ.nextDouble();
            if (servList.isEmpty()) {
                custWaits2.add(0.0);
                servList.addLast(cust);
                new Departure(cust, 2).schedule(cust.servTime);
            } else {
                waitList2.addLast(cust);
                totWait2.update(waitList2.size());
            }
        }
    }

    class Departure extends Event {
        Customer cust;
        int queueNumber;

        Departure(Customer cust, int queueNumber) {
            this.cust = cust;
            this.queueNumber = queueNumber;
        }

        public void actions() {
            servList.remove(cust);
            if (!waitList1.isEmpty() || !waitList2.isEmpty()) {
                Customer nextCust;
                if (!waitList1.isEmpty() && (waitList2.isEmpty() || Math.random() < probQueue1)) {
                    nextCust = waitList1.removeFirst();
                    totWait1.update(waitList1.size());
                    custWaits1.add(Sim.time() - nextCust.arrivTime);
                    servList.addLast(nextCust);
                    new Departure(nextCust, 1).schedule(nextCust.servTime);
                } else if (!waitList2.isEmpty()) {
                    nextCust = waitList2.removeFirst();
                    totWait2.update(waitList2.size());
                    custWaits2.add(Sim.time() - nextCust.arrivTime);
                    servList.addLast(nextCust);
                    new Departure(nextCust, 2).schedule(nextCust.servTime);
                }
            }
        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

    public static void main(String[] args) {
        double lambda1 = 1.0; // Taux d'arrivée pour la file 1
        double lambda2 = 1.0; // Taux d'arrivée pour la file 2
        double mu = 2.0; // Taux de service
        double probQueue1 = 0.7; // Probabilité de servir un client de la file 1
        TwoQueueSystem queue = new TwoQueueSystem(lambda1, lambda2, mu, probQueue1);
        queue.simulate(100000.0);
        System.out.println(queue.custWaits1.report());
        System.out.println(queue.custWaits2.report());
        System.out.println(queue.totWait1.report());
        System.out.println(queue.totWait2.report());
    }
}
