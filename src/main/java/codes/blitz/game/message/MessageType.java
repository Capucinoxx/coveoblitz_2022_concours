package codes.blitz.game.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType
{
    // @formatter:off
    @JsonProperty("COMMAND")
    COMMAND,
    @JsonProperty("REGISTER")
    REGISTER
    // @formatter:on

}
