<template>
    <v-card elevation="5" class="ml-10">
                <v-card-title class="primary white--text">Trusted List Details</v-card-title>
                <v-divider></v-divider>
                <v-row dense>
                    <v-col cols="5">
                        <v-list>
                            <v-subheader>Basic data</v-subheader>
                            <v-divider class="ml-2"></v-divider>
                            <v-list-item>
                              <v-list-item-title>Territory</v-list-item-title>
                              <v-list-item-subtitle>
                                 <v-chip class="pl-5 pr-5" color="primary" text-color="white">
                                    <h3>{{itemList.territory}}</h3>
                                 </v-chip>
                              </v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item>
                              <v-list-item-title>Territory country</v-list-item-title>
                              <v-list-item-subtitle><h3>{{getCountryName(itemList.territory)}}</h3></v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item>
                              <v-list-item-title>Operator name</v-list-item-title>
                              <v-list-item-subtitle><h3>{{itemList.operatorName}}</h3></v-list-item-subtitle>
                            </v-list-item>
                        </v-list>
                    </v-col>
                    <v-divider vertical class="mt-5 mb-5"></v-divider>
                    <v-col cols="4">
                        <v-list>
                          <v-subheader>Important Numbers and Dates</v-subheader>
                          <v-divider></v-divider>
                            <v-list-item>
                              <v-list-item-title>Sequence number</v-list-item-title>
                              <v-list-item-subtitle class="text-right">{{itemList.sequenceNumber}}</v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item>
                                <v-list-item-title>Issue date</v-list-item-title>
                                <v-list-item-subtitle class="text-right">{{itemList.listIssue | formatDate}}</v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item>
                                <v-list-item-title>Next update</v-list-item-title>
                                <v-list-item-subtitle class="text-right">{{itemList.nextUpdate | formatDate}}</v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item>
                                <v-list-item-title>Last Check</v-list-item-title>
                                <v-list-item-subtitle class="text-right">{{itemList.lastCheck | formatDate}}</v-list-item-subtitle>
                            </v-list-item>
                            
                        </v-list>
                    </v-col>
                    <v-divider vertical class="mt-5 mb-5"></v-divider>
                    <v-col >
                      <v-list>
                        <v-subheader>Remarks</v-subheader>
                        <v-divider class="mr-5"></v-divider>
                        <v-list-item>
                          <v-list-item-title>Status</v-list-item-title>
                            <v-list-item-subtitle>
                                <v-chip class="pl-5 pr-5" color="green" text-color="white" v-if="itemList.isValid">Valid</v-chip>
                                <v-chip class="pl-5 pr-5" color="red" text-color="white" v-else>Invalid</v-chip>
                            </v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item>
                          <v-list-item-title>Type</v-list-item-title>
                            <v-list-item-subtitle>
                              <v-chip class="pl-5 pr-5" color="purple" text-color="white">
                               {{itemList.type}}
                              </v-chip>
                            </v-list-item-subtitle>
                        </v-list-item>

                      </v-list>
                    </v-col>
                </v-row>
               
                
                <v-row dense>
                    <v-col cols="12">
                        <v-list>
                          <v-subheader class="ml-5">Common URIs</v-subheader>
                          <v-divider class="ml-5 mr-5"></v-divider>
                          <v-list-item two-line class="float-left">
                            <v-list-item-content>
                              <v-list-item-title>Distribution point URI</v-list-item-title>
                              <v-list-item-subtitle>
                               <a target="_new" :href="itemList.distributionPoint">{{itemList.distributionPoint}}</a>
                              </v-list-item-subtitle>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item two-line class="float-left">
                            <v-list-item-content>
                              <v-list-item-title>Information URI</v-list-item-title>
                              <v-list-item-subtitle>
                                <a target="new" :href="itemList.informationUri">{{itemList.informationUri}}</a>
                              </v-list-item-subtitle>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item two-line class="float-left">
                            <v-list-item-content>
                              <v-list-item-title>Local copy URI</v-list-item-title>
                              <v-list-item-subtitle>
                                <a href="">{{itemList.localUri}}</a>
                              </v-list-item-subtitle>
                            </v-list-item-content>
                          </v-list-item>
                          
                        </v-list>
                         
                    </v-col>
                </v-row>
                
                <v-subheader class="ml-5">Providers</v-subheader>
                <v-divider class="ml-5 mr-5"></v-divider>
                
                 <v-row>
                    <v-col cols="12">
                      
                      <v-expansion-panels multiple hover popout>
                        <v-expansion-panel
                          v-for="(item,i) in itemList.providers"
                          :key="i"
                        >
                        <v-expansion-panel-header class="blue lighten-5">{{item.name}}</v-expansion-panel-header>
                        <v-expansion-panel-content>
                        <v-row>
                          <v-col cols="12">
                            <v-list>
                              <v-list-item two-line class="float-left">
                                <v-list-item-content>
                                  <v-list-item-title>Name</v-list-item-title>
                                  <v-list-item-subtitle>{{item.name}}</v-list-item-subtitle>
                                </v-list-item-content>
                              </v-list-item>
                              <v-list-item two-line class="float-left">
                                <v-list-item-content>
                                  <v-list-item-title>Trade name</v-list-item-title>
                                  <v-list-item-subtitle>{{item.tradeName}}</v-list-item-subtitle>
                                </v-list-item-content>
                              </v-list-item>
                              <v-list-item two-line class="float-left">
                                <v-list-item-content>
                                  <v-list-item-title>Information URI</v-list-item-title>
                                    <v-list-item-subtitle>
                                      <a href=""> {{item.infoUri}}</a>
                                    </v-list-item-subtitle>
                                  </v-list-item-content>
                              </v-list-item>
                            </v-list>
                          </v-col>
                        </v-row>
                        <v-row >
                          <v-col></v-col>
                          <v-col cols="10">
                        <v-sheet elevation="1" color="grey lighten-5" class="mx-auto">
                          <v-subheader class="black--text">Services</v-subheader>
                          <v-slide-group
                            
                            class="pb-5"
                            active-class="success"
                            show-arrows="always"
                          >
                            <v-slide-item
                              v-for="s in item.services"
                              :key="s.id"
                              v-slot:default="{ active, toggle }"
                            >
                              <v-card
                                :color="active ? undefined : 'white'"
                                class="ma-3 fill-height"
                                height="292"
                                :width="s.type=='QC_CA' ? 288 : 450"
                                elevation="5"
                                @click="toggle"
                              >
                                <v-card-title>
                                    <v-tooltip top>
                                      <template v-slot:activator="{ on, attrs }">
                                      <span v-bind="attrs" v-on="on" class="text-caption font-weight-medium text-truncate">{{s.name}}</span>
                                      </template>
                                      <span >{{s.name}}</span>
                                    </v-tooltip>                          
                                </v-card-title>
                                <v-divider></v-divider>
                                <v-card-text class="pa-0">
                                  <v-list dense>
                                    <v-list-item>
                                      <v-list-item-content>
                                        <v-list-item-title>Start date</v-list-item-title>
                                        <v-list-item-subtitle>{{s.startDate | formatDate}}</v-list-item-subtitle>
                                      </v-list-item-content>
                                    </v-list-item>
                                    <v-list-item>
                                     
                                        <v-list-item-title>Cert Identities</v-list-item-title>
                                        <v-list-item-subtitle>{{s.certIdentities.length}}</v-list-item-subtitle>
                                      
                                    </v-list-item>
                                    <v-list-item>
                                      
                                        <v-list-item-title>Extensions</v-list-item-title>
                                        <v-list-item-subtitle>{{s.extensions.length}}</v-list-item-subtitle>
                                      
                                    </v-list-item>
                                     <v-list-item>
                                      
                                        <v-list-item-title>Supply point</v-list-item-title>
                                        <v-list-item-subtitle>{{s.supplyPoints.length}}</v-list-item-subtitle>
                                      
                                    </v-list-item>
                                  </v-list>
                                </v-card-text>
                                <v-divider></v-divider>
                                <v-card-actions>
                                  <v-chip color="orange" text-color="white" small class="ma-2"> 
                                    <v-avatar left>
                                        <v-icon>mdi-alpha-t-circle</v-icon>
                                    </v-avatar>
                                     {{s.type}}
                                  </v-chip>
                                  <v-chip color="green" text-color="white" small class="ma-2"> 
                                    <v-avatar left>
                                        <v-icon>mdi-alpha-s-circle</v-icon>
                                    </v-avatar>
                                     {{s.status}}
                                  </v-chip>
                                </v-card-actions>
                              </v-card>
                            </v-slide-item>
                          </v-slide-group>
                        </v-sheet>  
                          </v-col>
                          <v-col></v-col>
                        </v-row>
                      </v-expansion-panel-content>
                    </v-expansion-panel>
                  </v-expansion-panels>
                      
                </v-col>
                </v-row>
                <v-divider></v-divider>
                
                <v-card-actions>
                    <v-btn text color="primary" @click="hideListDetails">Close</v-btn>
                </v-card-actions>
            </v-card>
</template>

<script>
import moment from 'moment';
export default {
    name: 'TrustedListDetails',
    props: ['itemList'],
    data: () => ({
      isocountry: {
        'AT': 'Austria',
        'BE': 'Belgium',
        'BG': 'Bulgaria',
        'CY': 'Cyprus',
        'CZ': 'Czechia',
        'DE': 'Germany',
        'DK': 'Denmark',
        'EE': 'Estonia',
        'EL': 'Greece',
        'ES': 'Spain',
        'EU': 'European Union',
        'FI': 'Finland',
        'FR': 'France',
        'HR': 'Croatia',
        'HU': 'Hungary',
        'IE': 'Ireland',
        'IS': 'Iceland',
        'IT': 'Italy',
        'LI': 'Liechtenstein',
        'LT': 'Lithuania',
        'LU': 'Luxembourg',
        'LV': 'Latvia',
        'MT': 'Malta',
        'NL': 'Netherlands (the)',
        'NO': 'Norway',
        'PL': 'Poland',
        'PT': 'Portugal',
        'RO': 'Romania',
        'SE': 'Sweden',
        'SI': 'Slovenia',
        'SK': 'Slovakia',
        'UK': 'United Kingdom',
      }
    }),
    methods: {
        hideListDetails() {
          this.$emit('closeThis');
        },
        getCountryName(value) {
          return this.isocountry[value];
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