<template>
<v-sheet elevation="0" color="grey lighten-5" class="mx-auto">
  <v-container id="list-details">
    <v-row v-if="showd">
        <v-col cols="10" class="mx-auto">
            <trusted-list-details :itemList="itemList" v-on:closeThis="hideListDetails"/>
        </v-col>
    </v-row>
    <v-row class="float-left" dense>
      <v-col cols="3"
          v-for="item in lists"
        :key="item.territory"
      >
        <v-card elevation="5">
        <v-card-title class="primary white--text">{{item.territory}}</v-card-title>
        <v-card-subtitle class="primary white--text">
          <v-tooltip top>
            <template v-slot:activator="{ on, attrs }">
              <span v-bind="attrs" v-on="on" class="text-truncate">{{item.operatorName}}</span>
            </template>
            <span >{{item.operatorName}}</span>
          </v-tooltip>
        </v-card-subtitle>
        <v-divider></v-divider>

        <v-list dense>
          <v-list-item>
            <v-list-item-title>Sequence number</v-list-item-title>
            <v-list-item-subtitle class="text-right">{{item.sequenceNumber}}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
             <v-list-item-title>Issue date</v-list-item-title>
             <v-list-item-subtitle class="text-right">{{ item.listIssue | formatDate  }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>Next update</v-list-item-title>
            <v-list-item-subtitle class="text-right">{{item.nextUpdate | formatDate }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>Last Check</v-list-item-title>
            <v-list-item-subtitle class="text-right">{{item.lastCheck | formatDate }}</v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>Status</v-list-item-title>
            <v-list-item-subtitle class="text-right">
              <v-chip class="pl-5 pr-5" color="green" text-color="white" v-if="item.isValid">Valid</v-chip>
              <v-chip class="pl-5 pr-5" color="red" text-color="white" v-else>Invalid</v-chip>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
        <v-divider></v-divider>
        <v-card-actions>
          <v-btn text color="primary" @click="showListDetails(item)">Details</v-btn>
        </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>

  <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to load trust list ' + errors[0].message : '' }}

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
import TrustedListDetails from '@/components/TrustedListDetails.vue'
import api from '@/components/http-common.js'
import moment from 'moment';


  export default {
    name: 'TrustLists',
    components: { TrustedListDetails },
    
    data () {
      return {
        loading: true,
        errors: [],
        error: false,
        model: null,
        lists: [],    
        showd: false,
        itemList: null,
      }
    },
    created () {
      this.initialize()
    },
    methods: {
        showListDetails(item) {

           api.getSecured(`/trustlists/${item.territory}`)
            .then(response => {
              this.itemList = response.data;
              this.showd = true;
              this.scrollToElement();
          })
          .catch(e => {
            console.log(`/trustlists/${item.territory}: response failed: ${e}`);
            this.errors.push(e)
            this.error = true
          })
        },
        hideListDetails() {
            this.showd = false;
            this.itemList = null;
        },
        initialize () {
            api.getSecured(`/trustlists/brief`)
            .then(response => {
              this.lists = response.data;
              console.log(response.data);
              this.loading = false;
          })
          .catch(e => {
            console.log(`/trustlists/brief: response failed: ${e}`);
            this.errors.push(e)
            this.error = true
          })
        },
        scrollToElement() {
           window.scrollTo(0,0);
        }
    },
    filters: {
      formatDate(value) {
        if (value) {
          return moment(value).format('YYYY-MM-DD HH:mm:ss Z')
        }
      }
    },
  }
</script>