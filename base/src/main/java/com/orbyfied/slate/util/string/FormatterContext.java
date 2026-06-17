package com.orbyfied.slate.util.string;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The context/settings with which to format values.
 */
@RequiredArgsConstructor
@Getter
@Setter
public class FormatterContext {

    final FormatterContext parent;
    final FormatterRegistry registry;

}
