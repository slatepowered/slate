package com.orbyfied.slate.network.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Trust {

  UNAUTHORIZED(1024 * 16), /* very low packet size limit */

  INTERNAL(Integer.MAX_VALUE) /* high packet size limit, may transfer large amounts of data */

  ;

  private final int maxFrameSize;

}
