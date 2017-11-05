package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

import javax.vecmath.Point2f;

@PooledWeaver
@Transient
public class PathComponent extends Component {
	public Point2f targetPosition;
}
