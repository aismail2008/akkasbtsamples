package ActorFSM;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Created by aliismail on 24/08/2017.
 */
//Signals
class PowerOn {}
class PowerOff {}

//States
enum LightswitchState {on, off}

//Data
class NoDataItsJustALightswitch {}


public class Lightswitch extends AbstractFSM<LightswitchState, NoDataItsJustALightswitch> {
    {  //static initializer
        //okay, we're saying that when a new Lightswitch is born, it'll be in the off state and have a new NoDataItsJustALightswitch() object as data
        startWith(LightswitchState.off, new NoDataItsJustALightswitch());

        //our first FSM definition
        when(LightswitchState.off,                                //when in off,
                matchEvent(PowerOn.class,            //if we receive a PowerOn message,
                        NoDataItsJustALightswitch.class, //and have data of this type,
                        (powerOn, noData) ->             //we'll handle it using this function:
                                goTo(LightswitchState.on)                     //go to the on state,
                                        .replying(LightswitchState.on)           //and reply to the sender that we went to the on state
                )
        );

        //our second FSM definition
        when(LightswitchState.on,
                matchEvent(PowerOff.class,
                        (PowerOn, noData) ->
                                goTo(LightswitchState.off).replying(LightswitchState.off)
                        //here you could use multiline functions,
                        //and use the contents of the event (powerOn) or data (noData) to make decisions, alter content of the state, etc.

                )
        );

        initialize(); //boilerplate
    }

    public static Props props() {
        return Props.create(Lightswitch.class);
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("lightswitchtest");//should make this static if you're going to test a lot of things, actor systems are a bit expensive
        ActorRef lightswitch = system.actorOf(Props.create(Lightswitch.class)); //here is our lightswitch. It's an actor ref, a reference to an actor that will be created on
        //our behalf of type Lightswitch. We can't, as mentioned earlier, actually touch the instance
        //of Lightswitch, but we can send messages to it via this reference.

        lightswitch.tell(   //using the reference to our actor, tell it
                new PowerOn(),   //to "Power On," using our message type
                ActorRef.noSender());       //and giving it an actor to call back (in this case, the JavaTestKit itself)

        //because it is asynchronous, the tell will return immediately. Somewhere off in the distance, on another thread, our lightbulb is receiving its message

        lightswitch.tell(new PowerOff(), ActorRef.noSender());
    }
}

