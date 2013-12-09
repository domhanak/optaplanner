/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.config.heuristic.selector.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.collections.CollectionUtils;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.heuristic.selector.SelectorConfig;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.entity.descriptor.PlanningEntityDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.ComparatorSelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.FromSolutionEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.CachingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.FilteringEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.ProbabilityEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.ShufflingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.SortingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicRecordingEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;

@XStreamAlias("entitySelector")
public class EntitySelectorConfig extends SelectorConfig {

    @XStreamAsAttribute
    protected String id = null;
    @XStreamAsAttribute
    protected String mimicSelectorRef = null;

    protected Class<?> entityClass = null;

    protected SelectionCacheType cacheType = null;
    protected SelectionOrder selectionOrder = null;

    @XStreamImplicit(itemFieldName = "filterClass")
    protected List<Class<? extends SelectionFilter>> filterClassList = null;

    protected EntitySorterManner sorterManner = null;
    protected Class<? extends Comparator> sorterComparatorClass = null;
    protected Class<? extends SelectionSorterWeightFactory> sorterWeightFactoryClass = null;
    protected SelectionSorterOrder sorterOrder = null;
    protected Class<? extends SelectionSorter> sorterClass = null;

    protected Class<? extends SelectionProbabilityWeightFactory> probabilityWeightFactoryClass = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMimicSelectorRef() {
        return mimicSelectorRef;
    }

    public void setMimicSelectorRef(String mimicSelectorRef) {
        this.mimicSelectorRef = mimicSelectorRef;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public SelectionCacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(SelectionCacheType cacheType) {
        this.cacheType = cacheType;
    }

    public SelectionOrder getSelectionOrder() {
        return selectionOrder;
    }

    public void setSelectionOrder(SelectionOrder selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    public List<Class<? extends SelectionFilter>> getFilterClassList() {
        return filterClassList;
    }

    public void setFilterClassList(List<Class<? extends SelectionFilter>> filterClassList) {
        this.filterClassList = filterClassList;
    }

    public EntitySorterManner getSorterManner() {
        return sorterManner;
    }

    public void setSorterManner(EntitySorterManner sorterManner) {
        this.sorterManner = sorterManner;
    }

    public Class<? extends Comparator> getSorterComparatorClass() {
        return sorterComparatorClass;
    }

    public void setSorterComparatorClass(Class<? extends Comparator> sorterComparatorClass) {
        this.sorterComparatorClass = sorterComparatorClass;
    }

    public Class<? extends SelectionSorterWeightFactory> getSorterWeightFactoryClass() {
        return sorterWeightFactoryClass;
    }

    public void setSorterWeightFactoryClass(Class<? extends SelectionSorterWeightFactory> sorterWeightFactoryClass) {
        this.sorterWeightFactoryClass = sorterWeightFactoryClass;
    }

    public SelectionSorterOrder getSorterOrder() {
        return sorterOrder;
    }

    public void setSorterOrder(SelectionSorterOrder sorterOrder) {
        this.sorterOrder = sorterOrder;
    }

    public Class<? extends SelectionSorter> getSorterClass() {
        return sorterClass;
    }

    public void setSorterClass(Class<? extends SelectionSorter> sorterClass) {
        this.sorterClass = sorterClass;
    }

    public Class<? extends SelectionProbabilityWeightFactory> getProbabilityWeightFactoryClass() {
        return probabilityWeightFactoryClass;
    }

    public void setProbabilityWeightFactoryClass(Class<? extends SelectionProbabilityWeightFactory> probabilityWeightFactoryClass) {
        this.probabilityWeightFactoryClass = probabilityWeightFactoryClass;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    /**
     * @param configPolicy never null
     * @param minimumCacheType never null, If caching is used (different from {@link SelectionCacheType#JUST_IN_TIME}),
     * then it should be at least this {@link SelectionCacheType} because an ancestor already uses such caching
     * and less would be pointless.
     * @param inheritedSelectionOrder never null
     * @return never null
     */
    public EntitySelector buildEntitySelector(HeuristicConfigPolicy configPolicy,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder) {
        if (mimicSelectorRef != null) {
            return buildMimicReplaying(configPolicy);
        }
        PlanningEntityDescriptor entityDescriptor = deduceEntityDescriptor(
                configPolicy.getSolutionDescriptor(), entityClass);
        SelectionCacheType resolvedCacheType = SelectionCacheType.resolve(cacheType, minimumCacheType);
        SelectionOrder resolvedSelectionOrder = SelectionOrder.resolve(selectionOrder, inheritedSelectionOrder);

        validateCacheTypeVersusSelectionOrder(resolvedCacheType, resolvedSelectionOrder);
        validateSorting(resolvedSelectionOrder);
        validateProbability(resolvedSelectionOrder);

        // baseEntitySelector and lower should be SelectionOrder.ORIGINAL if they are going to get cached completely
        EntitySelector entitySelector = buildBaseEntitySelector(configPolicy, entityDescriptor,
                SelectionCacheType.max(minimumCacheType, resolvedCacheType),
                determineBaseRandomSelection(entityDescriptor, resolvedCacheType, resolvedSelectionOrder));

        entitySelector = applyFiltering(entityDescriptor, resolvedCacheType, resolvedSelectionOrder, entitySelector);
        entitySelector = applySorting(resolvedCacheType, resolvedSelectionOrder, entitySelector);
        entitySelector = applyProbability(resolvedCacheType, resolvedSelectionOrder, entitySelector);
        entitySelector = applyShuffling(resolvedCacheType, resolvedSelectionOrder, entitySelector);
        entitySelector = applyCaching(resolvedCacheType, resolvedSelectionOrder, entitySelector);
        entitySelector = applyMimicRecording(configPolicy, entitySelector);
        return entitySelector;
    }

    protected EntitySelector buildMimicReplaying(HeuristicConfigPolicy configPolicy) {
        if (id != null
                || entityClass != null
                || cacheType != null
                || selectionOrder != null
                || filterClassList != null
                || sorterManner != null
                || sorterComparatorClass != null
                || sorterWeightFactoryClass != null
                || sorterOrder != null
                || sorterClass != null
                || probabilityWeightFactoryClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") with mimicSelectorRef ("  + mimicSelectorRef
                    + ") has another property that is not null.");
        }
        MimicRecordingEntitySelector mimicRecordingEntitySelector
                = configPolicy.getMimicRecordingEntitySelector(mimicSelectorRef);
        if (mimicRecordingEntitySelector == null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has a mimicSelectorRef ("  + mimicSelectorRef
                    + ") for which no entitySelector with that id exists (in its solver phase).");
        }
        return new MimicReplayingEntitySelector(mimicRecordingEntitySelector);
    }

    protected boolean determineBaseRandomSelection(PlanningEntityDescriptor entityDescriptor,
            SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder) {
        switch (resolvedSelectionOrder) {
            case ORIGINAL:
                return false;
            case SORTED:
            case SHUFFLED:
            case PROBABILISTIC:
                // baseValueSelector and lower should be ORIGINAL if they are going to get cached completely
                return false;
            case RANDOM:
                // Predict if caching will occur
                return resolvedCacheType.isNotCached() || (isBaseInherentlyCached() && !hasFiltering(entityDescriptor));
            default:
                throw new IllegalStateException("The selectionOrder (" + resolvedSelectionOrder
                        + ") is not implemented.");
        }
    }

    protected boolean isBaseInherentlyCached() {
        return true;
    }

    private EntitySelector buildBaseEntitySelector(
            HeuristicConfigPolicy configPolicy, PlanningEntityDescriptor entityDescriptor,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        // FromSolutionEntitySelector caches by design, so it uses the minimumCacheType
        if (minimumCacheType.compareTo(SelectionCacheType.STEP) < 0) {
            // cacheType upgrades to SelectionCacheType.STEP (without shuffling) because JIT is not supported
            minimumCacheType = SelectionCacheType.STEP;
        }
        if (minimumCacheType == SelectionCacheType.SOLVER) {
            // TODO Solver cached entities are not compatible with DroolsScoreCalculator
            // because between phases the entities get cloned and the KieSession contains those clones afterwards
            // https://issues.jboss.org/browse/PLANNER-54
            throw new IllegalArgumentException("The minimumCacheType (" + minimumCacheType
                    + ") is not yet supported. Please use " + SelectionCacheType.PHASE + " instead.");
        }
        return new FromSolutionEntitySelector(entityDescriptor, minimumCacheType, randomSelection);
    }

    private boolean hasFiltering(PlanningEntityDescriptor entityDescriptor) {
        return !CollectionUtils.isEmpty(filterClassList)
                || entityDescriptor.hasMovableEntitySelectionFilter();
    }

    private EntitySelector applyFiltering(PlanningEntityDescriptor entityDescriptor,
            SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder,
            EntitySelector entitySelector) {
        if (hasFiltering(entityDescriptor)) {
            List<SelectionFilter> filterList = new ArrayList<SelectionFilter>(
                    filterClassList == null ? 1 : filterClassList.size() + 1);
            if (filterClassList != null) {
                for (Class<? extends SelectionFilter> filterClass : filterClassList) {
                    filterList.add(ConfigUtils.newInstance(this, "filterClass", filterClass));
                }
            }
            if (entityDescriptor.hasMovableEntitySelectionFilter()) {
                filterList.add(entityDescriptor.getMovableEntitySelectionFilter());
            }
            entitySelector = new FilteringEntitySelector(entitySelector, filterList);
        }
        return entitySelector;
    }

    private void validateSorting(SelectionOrder resolvedSelectionOrder) {
        if ((sorterManner != null || sorterComparatorClass != null || sorterWeightFactoryClass != null
                || sorterOrder != null || sorterClass != null)
                && resolvedSelectionOrder != SelectionOrder.SORTED) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") with sorterManner ("  + sorterManner
                    + ") and sorterComparatorClass ("  + sorterComparatorClass
                    + ") and sorterWeightFactoryClass ("  + sorterWeightFactoryClass
                    + ") and sorterOrder ("  + sorterOrder
                    + ") and sorterClass ("  + sorterClass
                    + ") has a resolvedSelectionOrder (" + resolvedSelectionOrder
                    + ") that is not " + SelectionOrder.SORTED + ".");
        }
        if (sorterManner != null && sorterComparatorClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterManner (" + sorterManner
                    + ") and a sorterComparatorClass (" + sorterComparatorClass + ").");
        }
        if (sorterManner != null && sorterWeightFactoryClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterManner (" + sorterManner
                    + ") and a sorterWeightFactoryClass (" + sorterWeightFactoryClass + ").");
        }
        if (sorterManner != null && sorterClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterManner (" + sorterManner
                    + ") and a sorterClass (" + sorterClass + ").");
        }
        if (sorterManner != null && sorterOrder != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") with sorterManner (" + sorterManner
                    + ") has a non-null sorterOrder (" + sorterOrder + ").");
        }
        if (sorterComparatorClass != null && sorterWeightFactoryClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterComparatorClass (" + sorterComparatorClass
                    + ") and a sorterWeightFactoryClass (" + sorterWeightFactoryClass + ").");
        }
        if (sorterComparatorClass != null && sorterClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterComparatorClass (" + sorterComparatorClass
                    + ") and a sorterClass (" + sorterClass + ").");
        }
        if (sorterWeightFactoryClass != null && sorterClass != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") has both a sorterWeightFactoryClass (" + sorterWeightFactoryClass
                    + ") and a sorterClass (" + sorterClass + ").");
        }
        if (sorterClass != null && sorterOrder != null) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") with sorterClass (" + sorterClass
                    + ") has a non-null sorterOrder (" + sorterOrder + ").");
        }
    }

    private EntitySelector applySorting(SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder,
            EntitySelector entitySelector) {
        if (resolvedSelectionOrder == SelectionOrder.SORTED) {
            SelectionSorter sorter;
            if (sorterManner != null) {
                sorter = sorterManner.determineSorter(entitySelector.getEntityDescriptor());
            } else if (sorterComparatorClass != null) {
                Comparator<Object> sorterComparator = ConfigUtils.newInstance(this,
                        "sorterComparatorClass", sorterComparatorClass);
                sorter = new ComparatorSelectionSorter(sorterComparator,
                        SelectionSorterOrder.resolve(sorterOrder));
            } else if (sorterWeightFactoryClass != null) {
                SelectionSorterWeightFactory sorterWeightFactory = ConfigUtils.newInstance(this,
                        "sorterWeightFactoryClass", sorterWeightFactoryClass);
                sorter = new WeightFactorySelectionSorter(sorterWeightFactory,
                        SelectionSorterOrder.resolve(sorterOrder));
            } else if (sorterClass != null) {
                sorter = ConfigUtils.newInstance(this, "sorterClass", sorterClass);
            } else {
                throw new IllegalArgumentException("The entitySelectorConfig (" + this
                        + ") with resolvedSelectionOrder ("  + resolvedSelectionOrder
                        + ") needs a sorterManner (" + sorterManner
                        + ") or a sorterComparatorClass (" + sorterComparatorClass
                        + ") or a sorterWeightFactoryClass (" + sorterWeightFactoryClass
                        + ") or a sorterClass (" + sorterClass + ").");
            }
            entitySelector = new SortingEntitySelector(entitySelector, resolvedCacheType, sorter);
        }
        return entitySelector;
    }

    private void validateProbability(SelectionOrder resolvedSelectionOrder) {
        if (probabilityWeightFactoryClass != null
                && resolvedSelectionOrder != SelectionOrder.PROBABILISTIC) {
            throw new IllegalArgumentException("The entitySelectorConfig (" + this
                    + ") with probabilityWeightFactoryClass (" + probabilityWeightFactoryClass
                    + ") has a resolvedSelectionOrder (" + resolvedSelectionOrder
                    + ") that is not " + SelectionOrder.PROBABILISTIC + ".");
        }
    }

    private EntitySelector applyProbability(SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder,
            EntitySelector entitySelector) {
        if (resolvedSelectionOrder == SelectionOrder.PROBABILISTIC) {
            if (probabilityWeightFactoryClass == null) {
                throw new IllegalArgumentException("The entitySelectorConfig (" + this
                        + ") with resolvedSelectionOrder (" + resolvedSelectionOrder
                        + ") needs a probabilityWeightFactoryClass ("
                        + probabilityWeightFactoryClass + ").");
            }
            SelectionProbabilityWeightFactory probabilityWeightFactory = ConfigUtils.newInstance(this,
                    "probabilityWeightFactoryClass", probabilityWeightFactoryClass);
            entitySelector = new ProbabilityEntitySelector(entitySelector,
                    resolvedCacheType, probabilityWeightFactory);
        }
        return entitySelector;
    }

    private EntitySelector applyShuffling(SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder,
            EntitySelector entitySelector) {
        if (resolvedSelectionOrder == SelectionOrder.SHUFFLED) {
            entitySelector = new ShufflingEntitySelector(entitySelector, resolvedCacheType);
        }
        return entitySelector;
    }

    private EntitySelector applyCaching(SelectionCacheType resolvedCacheType, SelectionOrder resolvedSelectionOrder,
            EntitySelector entitySelector) {
        if (resolvedCacheType.isCached() && resolvedCacheType.compareTo(entitySelector.getCacheType()) > 0) {
            entitySelector = new CachingEntitySelector(entitySelector, resolvedCacheType,
                    resolvedSelectionOrder.toRandomSelectionBoolean());
        }
        return entitySelector;
    }

    private EntitySelector applyMimicRecording(HeuristicConfigPolicy configPolicy, EntitySelector entitySelector) {
        if (id != null) {
            if (id.isEmpty()) {
                throw new IllegalArgumentException("The entitySelectorConfig (" + this
                        + ") has an empty id (" + id + ").");
            }
            MimicRecordingEntitySelector mimicRecordingEntitySelector
                    = new MimicRecordingEntitySelector(entitySelector);
            configPolicy.addMimicRecordingEntitySelector(id, mimicRecordingEntitySelector);
            entitySelector = mimicRecordingEntitySelector;
        }
        return entitySelector;
    }

    public void inherit(EntitySelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        id = ConfigUtils.inheritOverwritableProperty(id,
                inheritedConfig.getId());
        mimicSelectorRef = ConfigUtils.inheritOverwritableProperty(mimicSelectorRef,
                inheritedConfig.getMimicSelectorRef());
        entityClass = ConfigUtils.inheritOverwritableProperty(entityClass,
                inheritedConfig.getEntityClass());
        cacheType = ConfigUtils.inheritOverwritableProperty(cacheType, inheritedConfig.getCacheType());
        selectionOrder = ConfigUtils.inheritOverwritableProperty(selectionOrder, inheritedConfig.getSelectionOrder());
        filterClassList = ConfigUtils.inheritOverwritableProperty
                (filterClassList, inheritedConfig.getFilterClassList());
        sorterManner = ConfigUtils.inheritOverwritableProperty(
                sorterManner, inheritedConfig.getSorterManner());
        sorterComparatorClass = ConfigUtils.inheritOverwritableProperty(
                sorterComparatorClass, inheritedConfig.getSorterComparatorClass());
        sorterWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                sorterWeightFactoryClass, inheritedConfig.getSorterWeightFactoryClass());
        sorterOrder = ConfigUtils.inheritOverwritableProperty(
                sorterOrder, inheritedConfig.getSorterOrder());
        sorterClass = ConfigUtils.inheritOverwritableProperty(
                sorterClass, inheritedConfig.getSorterClass());
        probabilityWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                probabilityWeightFactoryClass, inheritedConfig.getProbabilityWeightFactoryClass());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entityClass + ")";
    }

    /**
     * Build-in ways of sorting.
     */
    public static enum EntitySorterManner {
        DECREASING_DIFFICULTY;

        public SelectionSorter determineSorter(PlanningEntityDescriptor entityDescriptor) {
            SelectionSorter sorter;
            switch (this) {
                case DECREASING_DIFFICULTY:
                    sorter = entityDescriptor.getDecreasingDifficultySorter();
                    if (sorter == null) {
                        throw new IllegalArgumentException("The sorterManner (" + this
                                + ") on entity class (" + entityDescriptor.getPlanningEntityClass()
                                + ") fails because that entity class's " + PlanningEntity.class.getSimpleName()
                                + " annotation does not declare any difficulty comparison.");
                    }
                    return sorter;
                default:
                    throw new IllegalStateException("The sorterManner ("
                            + this + ") is not implemented.");
            }
        }
    }

}
