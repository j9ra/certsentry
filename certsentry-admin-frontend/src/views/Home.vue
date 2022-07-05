<template>
  <v-sheet elevation="0" color="grey lighten-5" class="mx-auto">
    
    <v-container fluid>
      <v-row align-content="center">
        <v-col cols="3">
          <system-metric title="Users" msg="Number of system users" icon="mdi-account-details" :metric="users_cnt"/>
        </v-col>
        <v-col cols="3">
          <system-metric title="Profiles" msg="Number of system profiles" icon="mdi-tag-multiple" :metric="profiles_cnt"/>
        </v-col>
        <v-col cols="3">
          <system-metric title="Trust List" msg="Number of Trust list" icon="mdi-newspaper-variant-multiple-outline" :metric="trustlist_cnt"/>
        </v-col>
        <v-col cols="3">
          <system-metric title="Events" msg="Number of system events" icon="mdi-notebook-outline" :metric="events_cnt"/>
        </v-col>
      </v-row>
    
      <v-row>
        <v-col cols="7">
          <div class="ml-2">
            <validations-trend-chart/>
          </div>
        </v-col>
        <v-col cols="5">
          <div class="mr-2">
            <events-stats-chart/>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="2">
          <div class="ml-2">
            <top-users-applications/>
          </div>
        </v-col>
        <v-col cols="5" >
            <trust-list-updates/>
        </v-col>
        <v-col cols="5">
          <div class="mr-2">
            <trust-list-expires/>
          </div>
        </v-col>
      </v-row>
    </v-container>

    <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to load metrics ' + errors[0].message : '' }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="red"
          text
          v-bind="attrs"
          @click="error = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>

  </v-sheet>
</template>

<script>


import SystemMetric from '@/components/SystemMetric.vue'
import ValidationsTrendChart from '@/components/ValidationsTrendChart.vue'
import EventsStatsChart from '@/components/EventsStatsChart.vue'
import TrustListUpdates from '@/components/TrustListUpdates.vue'
import TrustListExpires from '@/components/TrustListExpires.vue'
import TopUsersApplications from '@/components/TopUsersApplications.vue'
import api from '@/components/http-common.js'

export default {
  name: 'Home',
  components: {
    SystemMetric, ValidationsTrendChart, EventsStatsChart, TrustListUpdates, TrustListExpires, TopUsersApplications
  },
  data: () => ({
    loading: true,
    users_cnt: 0,
    profiles_cnt: 0,
    trustlist_cnt: 0,
    events_cnt: 0,
    error: false,
    errors: [],
  }),
  created () {
      this.initialize()
    },
  methods: {
    initialize () {
        api.getSecured(`/system/metrics`)
         .then(response => {
           this.users_cnt = response.data.users;
           this.profiles_cnt = response.data.profiles;
           this.trustlist_cnt = response.data.trustLists;
           this.events_cnt = response.data.events;
           this.loading = false;
          })
          .catch(e => {
            console.log('/system/metrics: response error: ' + e);
            this.errors.push(e)
            this.error = true;
          })      
      },
  }
}
</script>

