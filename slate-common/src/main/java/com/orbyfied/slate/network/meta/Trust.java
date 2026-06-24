package com.orbyfied.slate.network.meta;

import com.orbyfied.slate.network.Connection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Trust {

  UNAUTHORIZED(1024 * 16), /* very low packet size limit */

  INTERNAL(Connection.MAX_FRAME_SIZE) /* high packet size limit, may transfer large amounts of data */

  ;

  private final int maxFrameSize;

}
