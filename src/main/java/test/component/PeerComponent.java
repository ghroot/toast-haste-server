package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class PeerComponent extends Component {
    public String name = "unknown";
}
