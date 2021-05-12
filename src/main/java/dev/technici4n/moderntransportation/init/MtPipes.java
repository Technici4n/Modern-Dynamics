package dev.technici4n.moderntransportation.init;

public class MtPipes {
    public enum Energy {
        LEAD("lead"),
        INVAR("invar"),
        ELECTRUM("electrum"),
        SIGNALUM("signalum"),
        ENDERIUM("enderium");

        public final String name;

        Energy(String name) {
            this.name = name;
        }
    }
}
