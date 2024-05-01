import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkiverse.homeassistant.runtime.model.EntityState;

public class Entity implements EntityCore {


    private String entityId;
    private IHAContext haContext;
    private EntityState state;

    public Entity(IHAContext ctx, String entityId) {
        this.entityId = entityId;
        this.haContext = ctx;
    }

    public String getArea() {
        return null; //haContext.getAreaFromEntityId().name();
    }

    @Override
    public IHAContext getContext() {
        return haContext;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }


}