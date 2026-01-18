package dev.nilswitt.webmap.views.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import dev.nilswitt.webmap.entities.AbstractEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.function.Consumer;

public abstract class EntityFilter<T extends AbstractEntity> {

    private T entityProbe;
    private Consumer<Example<T>> filter;
    private Logger logger = LogManager.getLogger(EntityFilter.class);

    public EntityFilter(Consumer<Example<T>> filter) {
        this.filter = filter;
        try {
            this.entityProbe = (T) ((Class<T>) ((java.lang.reflect.ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0]).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Failed to create entity probe instance", e);
            throw new RuntimeException(e);
        }
    }

    abstract ExampleMatcher buildMatcher();

    public Example<T> getExample() {
        return Example.of(this.entityProbe, buildMatcher());
    }

    public void update() {
        filter.accept(getExample());
    }

    abstract Component getComponent(String columnKey);

    T getEntityProbe() {
        return entityProbe;
    }

    public void setUp(Grid<T> grid){
        HeaderRow filterRow = grid.appendHeaderRow();
        for (Grid.Column<T> column : grid.getColumns()) {
            String columnKey = column.getKey();
            if (columnKey == null) {
                logger.warn("Column key is null for column: " + column.getId());
                continue;
            }
            logger.info("Column key: " + columnKey);
            Component filterComponent = getComponent(columnKey);
            filterRow.getCell(column).setComponent(filterComponent);
        }
    }
}