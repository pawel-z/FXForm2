/*
 * Copyright (c) 2012, dooApp <contact@dooapp.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of dooApp nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.dooapp.fxform.model.impl;

import com.dooapp.fxform.model.Element;
import com.dooapp.fxform.reflection.ReflectionUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Element based on a Field.
 * <p/>
 * Created at 15/10/12 08:47.<br>
 *
 * @author Antoine Mischler <antoine@dooapp.com>
 */
public abstract class AbstractFieldElement<SourceType, WrappedType> implements Element<WrappedType> {

    protected final Field field;

    private final ObjectProperty<SourceType> source = new SimpleObjectProperty<SourceType>();

    private List<ChangeListener> changeListeners = new LinkedList<ChangeListener>();

    private List<InvalidationListener> invalidationListeners = new LinkedList<InvalidationListener>();

    public AbstractFieldElement(Field field) {
        this.field = field;
        wrappedProperty().addListener(new ChangeListener<ObservableValue<WrappedType>>() {
            public void changed(ObservableValue<? extends ObservableValue<WrappedType>> observableValue, ObservableValue<WrappedType> wrappedTypeObservableValue, ObservableValue<WrappedType> wrappedTypeObservableValue1) {
                for (InvalidationListener invalidationListener : invalidationListeners) {
                    wrappedTypeObservableValue.removeListener(invalidationListener);
                    if (wrappedTypeObservableValue1 != null) {
                        wrappedTypeObservableValue1.addListener(invalidationListener);
                    }
                    invalidationListener.invalidated(observableValue);
                }
                for (ChangeListener changeListener : changeListeners) {
                    wrappedTypeObservableValue.removeListener(changeListener);
                    if (wrappedTypeObservableValue1 != null) {
                        wrappedTypeObservableValue1.addListener(changeListener);
                        changeListener.changed(observableValue, wrappedTypeObservableValue.getValue(), wrappedTypeObservableValue1.getValue());
                    }
                }
            }
        });
    }

    public Field getField() {
        return field;
    }

    public SourceType getSource() {
        return source.get();
    }

    public void setSource(SourceType source) {
        this.source.set(source);
    }

    public ObjectProperty<SourceType> sourceProperty() {
        return source;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    public void addListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
        wrappedProperty().getValue().addListener(changeListener);

    }

    public void removeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
        if (wrappedProperty().getValue() != null) {
            wrappedProperty().getValue().removeListener(changeListener);
        }
    }

    public WrappedType getValue() {
        if (wrappedProperty().getValue() != null) {
            return wrappedProperty().getValue().getValue();
        } else {
            return null;
        }
    }

    public void addListener(InvalidationListener invalidationListener) {
        invalidationListeners.add(invalidationListener);
        wrappedProperty().addListener(invalidationListener);
    }

    public void removeListener(InvalidationListener invalidationListener) {
        invalidationListeners.remove(invalidationListener);
        if (wrappedProperty().getValue() != null) {
            wrappedProperty().removeListener(invalidationListener);
        }
    }

    public void dispose() {
        source.unbind();
    }

    public Object getBean() {
        return getSource();
    }

    public String getName() {
        return field.getName();
    }

    protected abstract ObservableValue<ObservableValue<WrappedType>> wrappedProperty();

}