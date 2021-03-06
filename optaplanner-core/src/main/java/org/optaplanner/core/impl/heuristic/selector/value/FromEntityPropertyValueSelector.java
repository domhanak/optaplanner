/*
 * Copyright 2013 JBoss Inc
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

package org.optaplanner.core.impl.heuristic.selector.value;

import java.util.Iterator;

import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.impl.domain.valuerange.descriptor.PlanningValueRangeDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.PlanningVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.impl.phase.AbstractSolverPhaseScope;
import org.optaplanner.core.impl.solution.Solution;

/**
 * This is the common {@link ValueSelector} implementation.
 */
public class FromEntityPropertyValueSelector extends AbstractValueSelector {

    protected final PlanningValueRangeDescriptor valueRangeDescriptor;
    protected final boolean randomSelection;

    protected Solution workingSolution;

    public FromEntityPropertyValueSelector(PlanningValueRangeDescriptor valueRangeDescriptor,
            SelectionCacheType cacheType, boolean randomSelection) {
        this.valueRangeDescriptor = valueRangeDescriptor;
        this.randomSelection = randomSelection;
        if (cacheType.compareTo(SelectionCacheType.STEP) > 0) {
            throw new IllegalArgumentException("The selector (" + this
                    + ") does not support the cacheType (" + cacheType + ").");
        }
    }

    public PlanningVariableDescriptor getVariableDescriptor() {
        return valueRangeDescriptor.getVariableDescriptor();
    }

    @Override
    public void phaseStarted(AbstractSolverPhaseScope phaseScope) {
        super.phaseStarted(phaseScope);
        workingSolution = phaseScope.getWorkingSolution();
    }

    @Override
    public void phaseEnded(AbstractSolverPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        workingSolution = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isCountable() {
        return true; // TODO extract CountableValueRange
    }

    public boolean isNeverEnding() {
        return randomSelection || !isCountable();
    }

    public long getSize(Object entity) {
        ValueRange<?> valueRange = valueRangeDescriptor.extractValueRange(workingSolution, entity);
        return valueRange.getSize();
    }

    public Iterator<Object> iterator(Object entity) {
        ValueRange<Object> valueRange = (ValueRange<Object>)
                valueRangeDescriptor.extractValueRange(workingSolution, entity);
        if (!randomSelection) {
            return valueRange.createOriginalIterator();
        } else {
            return valueRange.createRandomIterator(workingRandom);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getVariableDescriptor().getVariableName() + ")";
    }

}
