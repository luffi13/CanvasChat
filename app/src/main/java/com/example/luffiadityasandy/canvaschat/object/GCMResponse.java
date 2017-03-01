package com.example.luffiadityasandy.canvaschat.object;

import java.util.List;

/**
 * Created by Luffi Aditya Sandy on 01/03/2017.
 */

public class GCMResponse {
    public String multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<GCMObject2> results;

    public class GCMObject2 {
        public int message_id;
        public int registration_id;
        public String error;
    }
}
