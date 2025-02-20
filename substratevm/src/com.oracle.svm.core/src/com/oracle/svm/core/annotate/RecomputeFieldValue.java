/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.hosted.FieldValueTransformer;

/**
 * Supported API is available to replace this non-API annotation: Use
 * {@link BeforeAnalysisAccess#registerFieldValueTransformer}.
 *
 * Mechanism to change the value of a field. Normally, field values in the native image heap of the
 * Substrate VM are just taken from the host VM. This annotation allows the field value to be
 * intercepted and recomputed.
 * <p>
 * This annotation must be used on a field also annotated with {@link Alias} to specify the field
 * whose value needs to be changed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Platforms(Platform.HOSTED_ONLY.class)
public @interface RecomputeFieldValue {

    enum Kind {
        /**
         * The initial field value is not modified. This is the default behavior when no
         * {@link RecomputeFieldValue} annotation is present.
         */
        None,
        /**
         * The field is reset to the default value (null, 0, false).
         */
        Reset,
        /**
         * The object field is set to a instance of {@link #declClass} created by calling the
         * default constructor.
         */
        NewInstance,
        /**
         * The object field is set to a instance of {@link #declClass} created by calling the
         * default constructor when the target field value is not null.
         */
        NewInstanceWhenNotNull,
        /**
         * The field is set to the value assigned to the {@link Alias} field.
         */
        FromAlias,
        /**
         * The int or long field is set to the offset of the field named {@link #name()} of the
         * class {@link #declClass}, as it would be computed by
         * {@link sun.misc.Unsafe#objectFieldOffset}.
         */
        FieldOffset,
        /**
         * The int or long field is set to the offset of the first array element of the array class
         * {@link #declClass}, as it would be computed by
         * {@link sun.misc.Unsafe#arrayBaseOffset(Class)}.
         */
        ArrayBaseOffset,
        /**
         * The int or long field is set to the element size array class {@link #declClass}, as it
         * would be computed by {@link sun.misc.Unsafe#arrayIndexScale(Class)}.
         */
        ArrayIndexScale,
        /**
         * The int or long field is set to the log2 of {@link #ArrayIndexScale}.
         */
        ArrayIndexShift,
        /**
         * Special support for field offsets used by
         * java.util.concurrent.atomic.AtomicXxxFieldUpdater.
         */
        AtomicFieldUpdaterOffset,
        /**
         * The field offset stored in this int or long field is updated. The original value must be
         * a valid field offset in the hosted universe, and the new value is the field offset of the
         * same field in the substrate universe. The field is looked up in the class
         * {@link #declClass}.
         */
        TranslateFieldOffset,
        /**
         * Marker value that the field is intercepted by some manual logic.
         */
        Manual,
        /**
         * Use a {@link FieldValueTransformer}, which is specified as the target class.
         */
        Custom,
    }

    /**
     * The kind of the recomputation performed.
     */
    Kind kind();

    /**
     * The class parameter for the recomputation. If this attribute is not specified, then the class
     * specified by {@link #declClassName()} is used. If neither {@link #declClass()} nor
     * {@link #declClassName()} is specified, then the class specified by the {@link TargetClass}
     * annotation is used.
     */
    Class<?> declClass() default RecomputeFieldValue.class;

    /**
     * The class parameter for the recomputation. If this attribute is not specified, then the class
     * specified by {@link #declClass()} is used. If neither {@link #declClass()} nor
     * {@link #declClassName()} is specified, then the class specified by the {@link TargetClass}
     * annotation is used.
     */
    String declClassName() default "";

    /**
     * The name parameter for the recomputation.
     */
    String name() default "";

    /**
     * Treat the value as final, to enforce constant folding already during static analysis.
     */
    boolean isFinal() default false;

    /**
     * If true, ignores previously computed values and calculates the value for every field read.
     */
    boolean disableCaching() default false;
}
