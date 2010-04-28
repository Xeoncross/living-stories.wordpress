/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.ui;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MapRightClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.Location;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.i18n.ClientConstants;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.util.Resources;

/**
 * Panel to input location information that includes latitude, longitude and a text description.
 * 
 * TODO: convert to UiBinder
 */
public class LocationInput extends Composite {
  public static ClientConstants properties = GWT.create(ClientConstants.class);
  
  private static final int MAP_HEIGHT = 256;
  private static final int MAP_WIDTH = 256;
  private static final int MAP_ZOOM = 10;
    
  private boolean mapsKeyExists;
  private boolean mapsApiReady = false;
  
  private TextBox latitudeTextBox;
  private TextBox longitudeTextBox;
  private TextArea locationDescriptionTextArea;
  private RadioButton useDisplayedLocation;
  private RadioButton useAlternateLocation;
  private TextBox alternateTextBox;
  private RadioButton useManualLatLong;
  private Button geocodeButton;
  private Label geocoderStatus;
  private MapWidget map;
  private Marker mapMarker;
  private DisclosurePanel locationZippy;
  
  private Hidden latitudeValue;
  private Hidden longitudeValue;
  
  public LocationInput() {
    this(null);
  }
  
  public LocationInput(Location location) {
    super();
    String mapsKey = properties.mapsKey();
    mapsKeyExists = mapsKey != null && !mapsKey.isEmpty();
    initWidget(createLocationPanel(location));
  }

  private Widget createLocationPanel(final Location location) {
    final VerticalPanel locationPanel = new VerticalPanel();
    
    if (mapsKeyExists) {
      HorizontalPanel descriptionPanel = new HorizontalPanel();
      descriptionPanel.add(new HTML("Location name (displayed to readers):"));
      locationDescriptionTextArea = new TextArea();
      locationDescriptionTextArea.setName("lsp_location_description");
      locationDescriptionTextArea.setCharacterWidth(50);
      locationDescriptionTextArea.setHeight("60px");
      descriptionPanel.add(locationDescriptionTextArea);

      Label geocodingOptions = new Label("Geocode based on:");
      useDisplayedLocation = new RadioButton("geoGroup", "The displayed location name");
      useDisplayedLocation.setValue(true);
      useAlternateLocation = new RadioButton("geoGroup",
      "An alternate location that geocodes better: ");
      alternateTextBox = new TextBox();
      alternateTextBox.setEnabled(false);
      HorizontalPanel alternatePanel = new HorizontalPanel();
      alternatePanel.add(useAlternateLocation);
      alternatePanel.add(alternateTextBox);
      useManualLatLong = new RadioButton("geoGroup",
      "Manually entered latitude and longitude numbers (enter these below)");

      HorizontalPanel latLongPanel = new HorizontalPanel();
      latLongPanel.add(new HTML("Latitude:&nbsp;"));
      latitudeTextBox = new TextBox();
      latitudeTextBox.setEnabled(false);
      latLongPanel.add(latitudeTextBox);
      latLongPanel.add(new HTML("&nbsp;Longitude:&nbsp;"));
      longitudeTextBox = new TextBox();
      longitudeTextBox.setName("lsp_longitude");
      longitudeTextBox.setEnabled(false);
      latLongPanel.add(longitudeTextBox);
      
      // Hidden fields are needed to pass the value of the latitude and longitude text boxes to 
      // the PHP code because the text boxes can be disabled. And if they are disabled, their
      // values can't be accessed.
      latitudeValue = new Hidden();
      latitudeValue.setName("lsp_latitude");
      longitudeValue = new Hidden();
      longitudeValue.setName("lsp_longitude");

      HorizontalPanel buttonPanel = new HorizontalPanel();
      geocodeButton = new Button("Geocode location");
      geocodeButton.setEnabled(false);
      buttonPanel.add(geocodeButton);
      geocoderStatus = new Label("");
      buttonPanel.add(geocoderStatus);    
      
      locationPanel.add(descriptionPanel);
      locationPanel.add(geocodingOptions);
      locationPanel.add(useDisplayedLocation);
      locationPanel.add(alternatePanel);
      locationPanel.add(useManualLatLong);
      locationPanel.add(latLongPanel);
      locationPanel.add(buttonPanel);
      locationPanel.add(new Label("Tip: once the map is visible, right-click a point on the map to"
          + " indicate that this is the precise location you want"));
      locationPanel.add(latitudeValue);
      locationPanel.add(longitudeValue);
      
      locationZippy = new DisclosurePanel("Location");
      locationZippy.add(locationPanel);
      
      // show a map based on geocoded or manually-inputted lat-long combination
      AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
      options.setOtherParms(properties.mapsKey() + "&sensor=false");
      AjaxLoader.loadApi("maps", "2", new Runnable() {
        @Override
        public void run() {
          mapsApiReady = true;
          map = new MapWidget();
          map.setSize(MAP_WIDTH + "px", MAP_HEIGHT + "px");
          map.addControl(new SmallMapControl());
          map.setDoubleClickZoom(true);
          map.setDraggable(true);
          map.setScrollWheelZoomEnabled(true);
          map.setZoomLevel(MAP_ZOOM);
          map.setVisible(false);
          locationPanel.add(map);
          createLocationHandlers();
          // Set the provided location on the map, if there is any
          setLocation(location);
          // Add handlers to re-center the map when the disclosure panel is toggled, because the
          // map has trouble centering if it's visibility is changed via the map. The handlers need
          // to be added here because we want to make sure that the map has been created before
          // adding the handlers.
          addDisclosurePanelHandlers();
        }
      }, options);
    } else {
      Label noKeyLabel = new Label("Google Maps API key not available. Please specify in" +
          " the ClientConstants.properties file.");
      noKeyLabel.setStyleName(Resources.INSTANCE.css().error());
      locationPanel.add(noKeyLabel);
    }
    
    return locationZippy;
  }
  
  private void addDisclosurePanelHandlers() {
    locationZippy.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      @Override
      public void onOpen(OpenEvent<DisclosurePanel> e) {
        locationZippy.setOpen(true);
        map.checkResizeAndCenter();
      }
    });
    locationZippy.addCloseHandler(new CloseHandler<DisclosurePanel>() {
      @Override
      public void onClose(CloseEvent<DisclosurePanel> e) {
        locationZippy.setOpen(false);
        map.checkResizeAndCenter();
      }
    });
  }
  
  public void setLocation(Location location) {
    if (mapsKeyExists && location != null) {
      Double latitude = location.getLatitude();
      Double longitude = location.getLongitude();
      String description = location.getDescription();
      latitudeTextBox.setText(latitude == null ? "" : latitude.toString());
      longitudeTextBox.setText(longitude == null ? "" : longitude.toString());
      if (latitude != null && longitude != null) {
        updateHiddenFieldValues();
        recenterMap();
      }
      locationDescriptionTextArea.setText(description == null ? "" : description);
      // Ensure that the state of the location controls are accurate.
      adjustLocationControls();
      controlGeocodeButton();
    }
  }
  
  private void updateHiddenFieldValues() {
    latitudeValue.setValue(latitudeTextBox.getText());
    longitudeValue.setValue(longitudeTextBox.getText());
  }

  /**
   * Creates event handlers for the Locations UI.
   */
  private void createLocationHandlers() {
    // first, set up interactions between the widgets:
    final ClickHandler radioHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        adjustLocationControls();
        controlGeocodeButton();
      }
    };
    
    final KeyUpHandler textHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        controlGeocodeButton();
      }
    };

    useDisplayedLocation.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        locationDescriptionTextArea.setFocus(true);
      }
    });
    
    useAlternateLocation.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        alternateTextBox.setFocus(true);
      }
    });

    useManualLatLong.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        latitudeTextBox.setFocus(true);
      }
    });

    locationDescriptionTextArea.addKeyUpHandler(textHandler);
    
    alternateTextBox.addKeyUpHandler(textHandler);
    
    latitudeTextBox.addKeyUpHandler(textHandler);
    longitudeTextBox.addKeyUpHandler(textHandler);
    
    // Actually handle the geocode button:
    geocodeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (useManualLatLong.getValue()) {
          // the latitude and longitude textboxes already have the right values in them
          recenterMap();
        } else {
          String address = (useDisplayedLocation.getValue() ? locationDescriptionTextArea
              : alternateTextBox).getText();
          geocoderStatus.setText("");
          new Geocoder().getLatLng(address, new LatLngCallback() {
            @Override
            public void onFailure() {
              geocoderStatus.setText("geocoding failed!");
            }

            @Override
            public void onSuccess(LatLng point) {
              geocoderStatus.setText("success");
              latitudeTextBox.setText(String.valueOf(point.getLatitude()));
              longitudeTextBox.setText(String.valueOf(point.getLongitude()));
              updateHiddenFieldValues();
              recenterMap();
            }
          });
        }
      }
    });
    
    map.addMapRightClickHandler(new MapRightClickHandler() {
      @Override
      public void onRightClick(MapRightClickEvent event) {
        LatLng clickedLatLng = map.convertContainerPixelToLatLng(event.getPoint());
        latitudeTextBox.setText(String.valueOf(clickedLatLng.getLatitude()));
        longitudeTextBox.setText(String.valueOf(clickedLatLng.getLongitude()));
        useManualLatLong.setValue(true);
        useManualLatLong.fireEvent(new ClickEvent() {});
        updateHiddenFieldValues();
        recenterMap();
      }
    });
  }
  
  private void adjustLocationControls() {
    alternateTextBox.setEnabled(useAlternateLocation.getValue());
    boolean manualLatLong = useManualLatLong.getValue();
    latitudeTextBox.setEnabled(manualLatLong);
    longitudeTextBox.setEnabled(manualLatLong);
  }
  
  private void controlGeocodeButton() {
    if (!mapsApiReady) {
      geocodeButton.setEnabled(false);
    } else if (useDisplayedLocation.getValue()) {
      geocodeButton.setEnabled(!locationDescriptionTextArea.getText().isEmpty());
      geocodeButton.setText("Geocode location");
    } else if (useAlternateLocation.getValue()) {
      geocodeButton.setEnabled(!alternateTextBox.getText().isEmpty());
      geocodeButton.setText("Geocode location");
    } else {
      geocodeButton.setEnabled(!latitudeTextBox.getText().isEmpty()
          && !longitudeTextBox.getText().isEmpty());
      geocodeButton.setText("Map location");
    }
  }
  
  private void recenterMap() {
    try {
      LatLng target = LatLng.newInstance(
          Double.parseDouble(latitudeTextBox.getText()),
          Double.parseDouble(longitudeTextBox.getText()));
      if (map.isVisible()) {
        map.panTo(target);
      } else {
        map.setVisible(true);
        map.setCenter(target);
        map.checkResizeAndCenter();
        // checkResizeAndCenter() call added per comments in
        // http://code.google.com/p/gwt-google-apis/issues/detail?id=223
      }
      if (mapMarker == null) {
        mapMarker = new Marker(target);
        map.addOverlay(mapMarker);
      } else {
        mapMarker.setLatLng(target);
      }
    } catch (NumberFormatException e) {
      geocoderStatus.setText("invalid latitude or longitude");
      map.setVisible(false);
    }
    // Make the copyright text smaller so it fits in the map.
    // This doesn't seem to work if it's set right when the map is created, so do it here.
    map.getElement().getFirstChildElement().getNextSiblingElement()
        .getStyle().setProperty("fontSize", "xx-small");
  }
}
